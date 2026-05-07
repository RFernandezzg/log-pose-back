package com.optcg.deckbuilder.service;

import com.optcg.deckbuilder.model.entity.Order;
import com.optcg.deckbuilder.model.entity.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Async
    public void sendOrderReceipt(String recipientEmail, Order order) {
        try {
            log.info("Iniciando envío de correo para el pedido #{}", order.getId());

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("OPTCG Deck Builder - Recibo de tu pedido #" + order.getId());

            String htmlContent = buildHtmlReceipt(order);

            if (htmlContent == null || htmlContent.isEmpty()) {
                log.error("El contenido HTML generado está vacío");
                return;
            }

            message.setContent(htmlContent, "text/html; charset=utf-8");

            javaMailSender.send(message);
            log.info("¡Correo enviado con éxito a {}!", recipientEmail);

        } catch (Exception e) {
            log.error("Error crítico al enviar el correo: {}", e.getMessage(), e);
        }
    }


    private String buildHtmlReceipt(Order order) {
        StringBuilder itemsHtml = new StringBuilder();

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                itemsHtml.append(String.format(
                        "<tr style=\"border-bottom: 1px solid rgba(255,255,255,0.1);\">" +
                                "  <td style=\"padding: 12px 0; color: #cbd5e1;\">%s</td>" +
                                "  <td style=\"padding: 12px 0; text-align: center; color: #cbd5e1;\">%d</td>" +
                                "  <td style=\"padding: 12px 0; text-align: right; color: #cbd5e1;\">%.2f &euro;</td>" +
                                "</tr>",
                        item.getItem().getName(),
                        item.getQuantity(),
                        item.getUnitPrice().doubleValue() * item.getQuantity()
                ));
            }
        }

        return String.format(
                "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                        "<style>" +
                        "  body { font-family: sans-serif; background-color: #0b0d2a; color: #f8fafc; margin: 0; padding: 20px; }" +
                        "  .container { max-width: 600px; margin: 0 auto; background-color: #1d2269; border-radius: 16px; padding: 30px; border: 1px solid #857752; }" +
                        "  h1 { color: #857752; text-transform: uppercase; }" +
                        "  table { width: 100%%; border-collapse: collapse; }" +
                        "  th { text-align: left; color: #857752; border-bottom: 1px solid #857752; }" +
                        "  .footer { margin-top: 30px; font-size: 12px; color: #64748b; text-align: center; }" +
                        "</style></head>" +
                        "<body>" +
                        "  <div class='container'>" +
                        "    <h1>Recibo de Compra</h1>" +
                        "    <p>Hola <strong>%s</strong>,</p>" +
                        "    <p>Pedido #%d confirmado.</p>" +
                        "    <table>" +
                        "      <thead><tr><th>Artículo</th><th>Cant.</th><th>Total</th></tr></thead>" +
                        "      <tbody>%s</tbody>" +
                        "    </table>" +
                        "    <p>Total: <strong>%.2f &euro;</strong></p>" +
                        "    <div class='footer'>OPTCG Deck Builder - Proyecto TFG</div>" +
                        "  </div>" +
                        "</body></html>",
                order.getUser().getUsername(),
                order.getId(),
                itemsHtml.toString(),
                order.getTotal().doubleValue()
        );
    }


    /*
    private String buildHtmlReceipt(Order order) {
        StringBuilder itemsHtml = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            itemsHtml.append(String.format(
                "<tr style=\"border-bottom: 1px solid rgba(255,255,255,0.1);\">" +
                "  <td style=\"padding: 12px 0; color: #cbd5e1;\">%s</td>" +
                "  <td style=\"padding: 12px 0; text-align: center; color: #cbd5e1;\">%d</td>" +
                "  <td style=\"padding: 12px 0; text-align: right; color: #cbd5e1;\">%.2f €</td>" +
                "</tr>",
                item.getItem().getName(),
                item.getQuantity(),
                item.getUnitPrice().doubleValue() * item.getQuantity()
            ));
        }

        return String.format(
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "  <style>" +
            "    body { font-family: 'Inter', sans-serif; background-color: #0b0d2a; color: #f8fafc; margin: 0; padding: 20px; }" +
            "    .container { max-width: 600px; margin: 0 auto; background-color: #1d2269; border-radius: 16px; padding: 30px; border: 1px solid rgba(133, 119, 82, 0.3); }" +
            "    h1 { color: #857752; font-size: 24px; font-weight: 900; margin-top: 0; text-transform: uppercase; letter-spacing: 2px; }" +
            "    p { color: #94a3b8; font-size: 14px; line-height: 1.6; }" +
            "    .highlight { color: #f8fafc; font-weight: bold; }" +
            "    table { width: 100%; border-collapse: collapse; margin-top: 20px; margin-bottom: 20px; }" +
            "    th { text-align: left; padding-bottom: 10px; color: #857752; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid rgba(133, 119, 82, 0.3); }" +
            "    .total-row { border-top: 2px solid #857752; font-weight: bold; font-size: 18px; }" +
            "    .footer { margin-top: 30px; font-size: 12px; color: #64748b; text-align: center; }" +
            "  </style>" +
            "</head>" +
            "<body>" +
            "  <div class=\"container\">" +
            "    <h1>Recibo de Compra</h1>" +
            "    <p>Hola <span class=\"highlight\">%s</span>,</p>" +
            "    <p>Gracias por tu pedido en <strong>OPTCG Deck Builder Store</strong>. Este es un comprobante de tu compra simulada (Pedido #%d).</p>" +
            "    " +
            "    <table>" +
            "      <thead>" +
            "        <tr>" +
            "          <th>Artículo</th>" +
            "          <th style=\"text-align: center;\">Cant.</th>" +
            "          <th style=\"text-align: right;\">Total</th>" +
            "        </tr>" +
            "      </thead>" +
            "      <tbody>" +
            "        %s" +
            "        <tr class=\"total-row\">" +
            "          <td colspan=\"2\" style=\"padding-top: 15px; color: #f8fafc;\">Total Estimado</td>" +
            "          <td style=\"padding-top: 15px; text-align: right; color: #857752;\">%.2f €</td>" +
            "        </tr>" +
            "      </tbody>" +
            "    </table>" +
            "    " +
            "    <p>Recuerda que esto es un proyecto universitario (TFG) y no se ha realizado ningún cobro real a tu cuenta.</p>" +
            "    " +
            "    <div class=\"footer\">" +
            "      &copy; 2026 OPTCG Deck Builder. Proyecto TFG DAW.<br>Todos los derechos reservados." +
            "    </div>" +
            "  </div>" +
            "</body>" +
            "</html>",
            order.getUser().getUsername(),
            order.getId(),
            itemsHtml.toString(),
            order.getTotal().doubleValue()
        );
    } */
}
