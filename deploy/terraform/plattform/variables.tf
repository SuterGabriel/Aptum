variable "kubeconfig" {
  description = "Pfad zur kubeconfig. Lokal die Standarddatei, in der CI die von kind geschriebene."
  type        = string
  default     = "~/.kube/config"
}

variable "kube_context" {
  description = "Der Kontext im kubeconfig. kind nennt ihn kind-<name>."
  type        = string
  default     = "kind-aptum"
}

variable "namespace" {
  description = "Wohin ArgoCD die Anwendung ausrollt."
  type        = string
  default     = "aptum"
}

variable "registry_host" {
  description = "Host der Registry für das Pull-Secret."
  type        = string
  default     = "ghcr.io"
}

variable "registry_user" {
  description = "Benutzer für das Pull-Secret."
  type        = string
  default     = ""
}

variable "registry_token" {
  description = "Token für das Pull-Secret. Kommt aus der Umgebung (TF_VAR_registry_token), steht in keiner Datei."
  type        = string
  default     = ""
  sensitive   = true
}

variable "argocd_chart_version" {
  description = "Version des argo-cd-Charts. Festgenagelt; Dependabot kennt Terraform-Charts nicht, also steht hier bewusst eine Zahl, die jemand anheben muss."
  type        = string
  default     = "10.9.0"
}
