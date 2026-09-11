variable "kubeconfig" {
  description = "Pfad zur kubeconfig."
  type        = string
  default     = "~/.kube/config"
}

variable "kube_context" {
  description = "Der Kontext im kubeconfig."
  type        = string
  default     = "kind-aptum"
}

variable "namespace" {
  description = "Wohin ArgoCD die Anwendung ausrollt - der Namespace aus plattform/."
  type        = string
  default     = "aptum"
}

variable "repo_url" {
  description = "Das Repo, aus dem ArgoCD das Chart liest."
  type        = string
  default     = "https://github.com/SuterGabriel/Aptum.git"
}

variable "revision" {
  description = "Zweig oder Commit. Die Pipeline übergibt den Commit, den sie gerade prüft - nicht main, sonst prüft sie den Stand von vorhin."
  type        = string
  default     = "main"
}

variable "image_registry" {
  description = "Registry der drei Bilder, etwa ghcr.io/sutergabriel. Leer: lokal geladene Bilder, pullPolicy Never."
  type        = string
  default     = ""
}

variable "image_tag" {
  description = "Tag der drei Bilder. Die Pipeline nimmt den Commit-Hash; lokal latest."
  type        = string
  default     = "latest"
}

variable "pull_secret" {
  description = "Name des Pull-Secrets aus plattform/, oder leer."
  type        = string
  default     = ""
}
