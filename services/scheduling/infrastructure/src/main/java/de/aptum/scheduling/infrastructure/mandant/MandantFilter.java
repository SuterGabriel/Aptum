package de.aptum.scheduling.infrastructure.mandant;

import de.aptum.scheduling.application.mandant.MandantId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Liest den Mandanten aus der Anfrage und bindet ihn an den Thread, für die
 * Dauer der Anfrage.
 *
 * <p><strong>Der Header {@code X-Mandant} ist ein Platzhalter.</strong> In
 * einer echten Anwendung käme der Mandant aus einem signierten Token, nicht
 * aus einem Header, den jeder setzen kann. Er steht hier, damit die Kette
 * Anfrage → Kontext → Transaktion → Policy einmal vollständig läuft; die
 * Authentifizierung ist in {@code docs/OFFENE-PUNKTE.md} als offen geführt.
 *
 * <p>Ohne Header gibt es keinen Mandanten, also keine Transaktion, also
 * nichts — die Antwort ist 400, nicht ein Standardmandant.
 */
@Component
class MandantFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Mandant";

    private final MandantKontextHalter halter;

    MandantFilter(MandantKontextHalter halter) {
        this.halter = halter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Health und die Schnittstellenbeschreibung sind mandantenfrei: Sie
        // sagen, was die Anwendung kann, nicht, was sie für jemanden weiß.
        String pfad = request.getRequestURI();
        return pfad.startsWith("/actuator") || pfad.startsWith("/v3/api-docs") || pfad.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String wert = request.getHeader(HEADER);
        if (wert == null || wert.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"fehler\":\"Kopfzeile X-Mandant fehlt\"}");
            return;
        }
        try {
            halter.als(new MandantId(wert.trim()), () -> {
                try {
                    chain.doFilter(request, response);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } catch (ServletException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
