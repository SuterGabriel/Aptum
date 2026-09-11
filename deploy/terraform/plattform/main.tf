# Stand 1 von 2: die Plattform. Was einmal stehen muss, damit etwas laufen
# kann - der Namespace, das Pull-Secret für die Registry und ArgoCD selbst.
#
# Zwei Werkzeuge, zwei Aufgaben, und die Grenze ist Absicht:
#
#   Terraform  - richtet den Mechanismus ein, der die Anwendung ausrollt.
#   ArgoCD     - rollt die Anwendung aus und hält sie nach: Es liest das
#                Helm-Chart unter deploy/helm/aptum aus Git und gleicht den
#                Cluster damit ab. Wer deployen will, pusht.
#
# Warum zwei Terraform-Stände (plattform/ und anwendung/) und nicht einer:
# Die ArgoCD-Application ist eine Ressource vom Typ, den erst ArgoCD in den
# Cluster bringt. Terraform plant alles vorab und kann eine Ressource nicht
# planen, deren Typ es noch nicht gibt; Helm prüft alle Manifeste gegen die
# API, bevor es CRDs installiert, also geht es auch nicht über das Chart.
# Zwei Stände in fester Reihenfolge lösen das ohne Trick - und sie bilden
# ab, was in einem echten Betrieb ohnehin getrennt ist: die Plattform, die
# ein Team einmal stellt, und die Anmeldung einer Anwendung an ihr.
#
# Der Cluster selbst ist hier eine Voraussetzung, kein Ergebnis: Lokal und in
# der CI ein kind-Cluster aus deploy/kind/cluster.yaml. In einer Cloud stünde
# davor ein dritter Stand, der den Cluster anlegt (EKS, AKS, GKE) - und diese
# beiden blieben unverändert.

terraform {
  required_version = ">= 1.9"
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.35"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 3.0"
    }
  }
}

provider "kubernetes" {
  config_path    = var.kubeconfig
  config_context = var.kube_context
}

provider "helm" {
  kubernetes = {
    config_path    = var.kubeconfig
    config_context = var.kube_context
  }
}

# ---- Namespace der Anwendung ---------------------------------------------

resource "kubernetes_namespace" "aptum" {
  metadata {
    name = var.namespace
    labels = {
      "app.kubernetes.io/name"       = "aptum"
      "app.kubernetes.io/managed-by" = "terraform"
    }
  }
}

# ---- Pull-Secret für die Registry ---------------------------------------
# Die Bilder liegen in der GitHub Container Registry, und ein frisch
# angelegtes Paket ist dort privat. Der Cluster braucht also einen Zugang -
# hier das Token der Pipeline, das nach dem Lauf verfällt. Ohne Token (lokal,
# mit geladenen Bildern) wird kein Secret angelegt.

resource "kubernetes_secret" "registry" {
  count = var.registry_token == "" ? 0 : 1
  metadata {
    name      = "registry"
    namespace = kubernetes_namespace.aptum.metadata[0].name
  }
  type = "kubernetes.io/dockerconfigjson"
  data = {
    ".dockerconfigjson" = jsonencode({
      auths = {
        (var.registry_host) = {
          username = var.registry_user
          password = var.registry_token
          auth     = base64encode("${var.registry_user}:${var.registry_token}")
        }
      }
    })
  }
}

# ---- ArgoCD -------------------------------------------------------------
# Schmal installiert: kein Dex, keine Notifications, ein Replica. Es soll
# synchronisieren, nicht beeindrucken. Die Version ist festgenagelt - ein
# Chart, das sich von selbst aktualisiert, ist keine Infrastruktur, sondern
# eine Überraschung.

resource "helm_release" "argocd" {
  name             = "argocd"
  repository       = "https://argoproj.github.io/argo-helm"
  chart            = "argo-cd"
  version          = var.argocd_chart_version
  namespace        = "argocd"
  create_namespace = true
  wait             = true
  timeout          = 600

  values = [yamlencode({
    dex            = { enabled = false }
    notifications  = { enabled = false }
    applicationSet = { enabled = false }
    server = {
      extraArgs = ["--insecure"]
      resources = { requests = { cpu = "50m", memory = "128Mi" } }
    }
    controller = {
      resources = { requests = { cpu = "100m", memory = "256Mi" } }
    }
    repoServer = {
      resources = { requests = { cpu = "50m", memory = "128Mi" } }
    }
    redis = {
      resources = { requests = { cpu = "50m", memory = "64Mi" } }
    }
    configs = {
      params = { "server.insecure" = true }
    }
  })]
}

output "namespace" {
  description = "Wo die Anwendung laufen wird - der zweite Stand braucht den Namen."
  value       = kubernetes_namespace.aptum.metadata[0].name
}

output "pull_secret" {
  description = "Name des Pull-Secrets, oder leer."
  # Aus der Ressource abgeleitet, nicht aus dem Token: Sonst gilt der Output
  # als sensibel, obwohl er nur einen Namen trägt.
  value       = length(kubernetes_secret.registry) > 0 ? "registry" : ""
}
