package com.appmusicale.controller.member.homebar.centre;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.awt.image.BufferedImage;
import java.io.File;

public class DocumentController {

    @FXML private ScrollPane pdfScrollPane;
    @FXML private VBox pdfContainer;

    public void loadPdf(String path) {
        try (PDDocument document = PDDocument.load(new File(path))) {
            PDFRenderer renderer = new PDFRenderer(document); //per trasformare pagine in immagini
            pdfContainer.getChildren().clear();

            double maxWidth = 600; //larghezza massima finestra
            int pageCount = document.getNumberOfPages(); //conta pagine

            for (int page = 0; page < pageCount; page++) {
                BufferedImage bim = renderer.renderImageWithDPI(page, 150); //150 dpi(qualità)
                //conversione in immagine
                ImageView imageView = new ImageView(SwingFXUtils.toFXImage(bim, null));

                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);

                //scarica la pagina e la aggiunge al container
                imageView.setFitWidth(maxWidth);
                pdfContainer.getChildren().add(imageView);

                //dimensioni finestra dipendono da quelle della prima pagine
                if (page == 0) {
                    pdfScrollPane.setPrefViewportWidth(maxWidth);

                    if (pageCount == 1) {
                        //se ho solo una pagina niente scroll
                        pdfScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                        double imgHeight = imageView.getImage().getHeight()
                                * (maxWidth / imageView.getImage().getWidth());

                        pdfScrollPane.setPrefViewportHeight(imgHeight);
                    } else {
                        pdfScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
