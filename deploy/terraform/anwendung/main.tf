# Stand 2 von 2: die Anmeldung der Anwendung bei ArgoCD.
#
# Eine einzige Ressource: die Application, die auf dieses Repo und den Pfad
# deploy/helm/aptum zeigt. ArgoCD liest das Chart aus dem Commit, den die
# Pipeline gerade prüft, und gleicht den Namespace damit ab. Die Bild-Tags
# kommen aus dem Pipeline-Lauf, alles andere aus values.yaml im Repo.
#
# selfHeal: Wer im Cluster von Hand etwas ändert, sieht es beim nächsten
# Abgleich wieder so, wie es in Git steht. prune: Was aus dem Chart
# verschwindet, verschwindet aus dem Cluster. Das ist der Punkt von GitOps -
# der Cluster ist eine Ableitung des Repos, nicht umgekehrt.
#
# Setzt voraus, dass plattform/ angewendet ist: Der Typ Application existiert
# erst, wenn ArgoCD im Cluster ist. Siehe dort, warum das zwei Stände sind.

terraform {
  required_version = ">= 1.9"
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.35"
    }
  }
}

provider "kubernetes" {
  config_path    = var.kubeconfig
  config_context = var.kube_context
}

resource "kubernetes_manifest" "aptum" {
  manifest = {
    apiVersion = "argoproj.io/v1alpha1"
    kind       = "Application"
    metadata = {
      name      = "aptum"
      namespace = "argocd"
      # Räumt beim Löschen der Application auch das Deployte weg.
      finalizers = ["resources-finalizer.argocd.argoproj.io"]
    }
    spec = {
      project = "default"
      source = {
        repoURL        = var.repo_url
        targetRevision = var.revision
        path           = "deploy/helm/aptum"
        helm = {
          valuesObject = {
            bilder = {
              registry   = var.image_registry
              tag        = var.image_tag
              pullPolicy = var.image_registry == "" ? "Never" : "IfNotPresent"
              pullSecret = var.pull_secret
            }
          }
        }
      }
      destination = {
        server    = "https://kubernetes.default.svc"
        namespace = var.namespace
      }
      syncPolicy = {
        automated = {
          prune    = true
          selfHeal = true
        }
        syncOptions = ["CreateNamespace=false"]
      }
    }
  }

  # ArgoCD schreibt in den Status; Terraform soll das nicht als Drift lesen.
  computed_fields = ["metadata.finalizers", "status"]
}

output "zustand_abfragen" {
  description = "So fragt man ArgoCD, ob es synchron und gesund ist."
  value       = "kubectl get application aptum -n argocd -o jsonpath='{.status.sync.status} {.status.health.status}'"
}
