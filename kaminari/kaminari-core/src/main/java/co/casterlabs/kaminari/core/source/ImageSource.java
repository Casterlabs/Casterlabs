package co.casterlabs.kaminari.core.source;

import java.awt.Graphics;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import co.casterlabs.kaminari.core.scene.Scene;
import lombok.NonNull;

public class ImageSource extends Source {
    private JComponent dummyComponent = new JComponent() {
        private static final long serialVersionUID = -6345831411588137979L;

        @Override
        public void paintComponent(Graphics g) {
            if (image != null) {
                int width = panel.getWidth();
                int height = panel.getHeight();

                g.drawImage(image, 0, 0, width, height, panel);
            }
        }
    };

    private Image image;

    public ImageSource(@NonNull Scene scene, @NonNull String id, @NonNull String name) {
        super(scene, id, name);

        this.panel.add(this.dummyComponent);
        this.dummyComponent.setSize(100000, 100000); // Any large number works.
    }

    public void setImageFromDataUri(String dataUri) throws IOException {
        if (dataUri == null) {
            this.image = null;
            return;
        }

        // data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==
        dataUri = dataUri.split("base64,")[1];

        byte[] bytes = Base64.getMimeDecoder().decode(dataUri);

        this.setImage(bytes);
    }

    public void setImage(byte[] bytes) throws IOException {
        if (bytes == null) {
            this.image = null;
            return;
        }

        this.image = ImageIO.read(new ByteArrayInputStream(bytes));
    }

    public int getAspectRatio() {
        if (this.image == null) {
            return 0;
        }

        return this.image.getHeight(null) / this.image.getWidth(null);
    }

}
