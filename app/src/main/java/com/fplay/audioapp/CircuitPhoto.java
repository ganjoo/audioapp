package com.fplay.audioapp;



import android.os.Parcel;
import android.os.Parcelable;

public class CircuitPhoto implements Parcelable {

    private static String ROOT_URL_CIRCUIT_IMAGES = "https://s3-ap-southeast-1.amazonaws.com/lucknowimages/";
    private String mUrl;
    private String mTitle;

    public CircuitPhoto(String url, String title) {
        mUrl = url;
        mTitle = title;
    }

    protected CircuitPhoto(Parcel in) {
        mUrl = in.readString();
        mTitle = in.readString();
    }

    public static final Creator<CircuitPhoto> CREATOR = new Creator<CircuitPhoto>() {
        @Override
        public CircuitPhoto createFromParcel(Parcel in) {
            return new CircuitPhoto(in);
        }

        @Override
        public CircuitPhoto[] newArray(int size) {
            return new CircuitPhoto[size];
        }
    };

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public static  CircuitPhoto[] getSpacePhotos(int circuit_number) {

        switch (circuit_number) {
            case 1:
                return new CircuitPhoto[]{
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C1/1+Shivalik.jpg", "Shivalik"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C1/3+Hastinapur.jpg", "Hastinapur"),
//                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C1/1-Circuit+48x72.jpg", "Circuit"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C1/2+Amangarh.jpg", "Amangargh"),

                };
            case 2:
                return new CircuitPhoto[]{
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C2/4+Bear+Rescue.jpg", "Bear Rescue"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C2/7+Etawah+Safari+Park.jpg", "Etawah Safari Park"),
                        //new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C2/2-Circuit+36x72.jpg", "Circuit"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C2/5+Taj+Nature+Walk.jpg", "Taj Nature Walk"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C2/6+National+Chambal+Santuary.jpg", "National Chambal Santuary"),


                };
            case 3:
                return new CircuitPhoto[]{
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C3/9+Dudhwa.jpg", "Dudhwa"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C3/11+Kishanpur.jpg", "Kishanpur"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C3/8+Pilibhit.jpg", "Pilibhit"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C3/10+Katarniaghat.jpg", "Kataniaghat"),
                        //new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C3/3-Circuit+48x72.jpg", "Circuit"),


                };
            case 4:
                return new CircuitPhoto[]{
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C4/15+Kalinjar.jpg", "Kalinjar"),
                        //new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C4/4-Circuit+48x72.jpg", "Circuit"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C4/12+Mahavir+Swami.jpg", "Mahavir Swami"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C4/13+Devgarh.jpg", "Devgarh"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C4/14+Vijay+Sagar.jpg", "Vijay Sagar"),


                };
            case 5:
                return new CircuitPhoto[]{
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C5/17+Kaimur.jpg", "Kaimur"),
                        //new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C5/5-Circuit+36x72.jpg", "Circuit"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C5/16+Ranipur.jpg", "Ranipur"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C5/21+Hathinala.jpg", "Hathinala"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C5/19+Chandra+Prabha.jpg", "Chandra Prabha"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C5/20+Vijay+garh.jpg", "Vijay Garh"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C5/18+Chunar.jpg", "Chunar"),

                };
            case 7:
                return new CircuitPhoto[]{
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C7/29+Lakh+Babhosi.jpg", "Lakh Babhosi"),
                        //new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C7/7-Circuit+42x72.jpg", "Circuit"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C7/32+Samaspur+Wildlife+Sanctuary.jpg", "Samaspur"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C7/31+Shaheed+Chandrasekhar+Azad+Bird+Sanctuary.jpg", "Shaheed Chandrashekhar Azad Bird Sanctuary"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C7/30+Sandi+Wildlife+Sanctuary.jpg", "Sandi Wildlife Sanctuary"),


                };
            case 6:
                return new CircuitPhoto[]{
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C6/26+Saman.jpg", "Saman"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C6/27+Sarsai.jpg", "Sarsai"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C6/24+Shekha+Jheel.jpg", "Shekha Jheel"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C6/23+Surajpur+Wetland.jpg", "Surajpur"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C6/28+Soor+Sarovar.jpg", "Soor Sarovar"),
                        //new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C6/6-Circuit+48x72.jpg", "Circuit"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C6/25+Patna+Wildlife.jpg", "Patna Wildlife"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C6/22+Okhla.jpg", "Okhla"),

                };
            case 8:
                return new CircuitPhoto[]{
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C8/34+Kannauj.jpg", "Kannauj"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C8/33+Narora.jpg", "Narora"),
                        //new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C8/8-Circuit+42x72.jpg", "Circuit"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C8/35+Bithoor.jpg", "Bithoor"),

                };
            case 9:
                return new CircuitPhoto[]{
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C9/37+PArvati+Arga+Bird+Sanctuary.jpg", "Parvati Agra Bird Sanctuary"),
                        //new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C9/9-Circuit+42x72.jpg", "Circuit"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C9/36+Sohelwa+Wildlife+Sanctuary.jpg", "Sohewala Wildlife Sanctuary"),
                        new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C9/38+Sohagibarwa+Wildlife+Sanctuary.jpg", "Sohagibarwa Wildlife Sanctuary"),


                };
        }
        return new CircuitPhoto[]{
                new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C1/1+Shivalik.jpg", "Shivalik"),
                new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C1/3+Hastinapur.jpg", "Hastinapur"),
                //new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C1/1-Circuit+48x72.jpg", "Circuit"),
                new CircuitPhoto(ROOT_URL_CIRCUIT_IMAGES + "C1/2+Amangarh.jpg", "Amangargh"),

        };
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mUrl);
        parcel.writeString(mTitle);
    }
}