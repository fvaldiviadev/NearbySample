package pojo;

import java.io.Serializable;

/**
 * Created by Fran on 25/01/2018.
 */

public class DataTransfered implements Serializable {
    private String message;
    private String imageBase64;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}

