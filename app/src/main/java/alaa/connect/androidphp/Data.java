package alaa.connect.androidphp;

public class Data {

    private int id;
    private String username, imageHash, publicKey, description, signedData;

    public String getSignedData() {
        return signedData;
    }

    public Data(int id, String username, String imageHash, String publicKey, String description, String signedData) {
        this.id = id;
        this.username = username;
        this.imageHash = imageHash;
        this.publicKey = publicKey;
        this.description = description;
        this.signedData = signedData;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getImageHash() {
        return imageHash;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getDescription() {
        return description;
    }
}