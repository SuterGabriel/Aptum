{{/*
Das Bild einer Komponente: Registry, Name, Tag. Die Namen sind die aus compose,
damit "kind load docker-image aptum-scheduling:latest" ohne Umbenennen passt.
*/}}
{{- define "aptum.bild" -}}
{{- $registry := .Values.bilder.registry -}}
{{- if $registry }}{{ $registry }}/{{ end }}aptum-{{ .komponente }}:{{ .Values.bilder.tag }}
{{- end -}}

{{/*
Gemeinsame Labels - so, wie kubectl und Helm sie erwarten.
*/}}
{{- define "aptum.labels" -}}
app.kubernetes.io/name: aptum
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end -}}

{{/*
Der Selektor einer Komponente. Bewusst schmal: Nur was ein Pod dauerhaft trägt.
*/}}
{{- define "aptum.selektor" -}}
app.kubernetes.io/name: aptum
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: {{ .komponente }}
{{- end -}}
