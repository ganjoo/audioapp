package com.fplay.audioapp.utils;

import android.content.Context;

import com.fplay.audioapp.R;

/**
 * Created by User on 07-11-2016.
 */
public class Utils {


    public static String getTextFromOCRTag(String tracker_name, Context ctx) {
        switch(tracker_name) {

            case "Barking Deer":
                return ctx.getResources().getString(R.string.info_barkingdear);
            case "Sarus Crane":
                return ctx.getResources().getString(R.string.info_saras_crane);

            case "Sambar Deer":
                return ctx.getResources().getString(R.string.info_sambar_hiran);
            case "Rosy Pelican":
                return ctx.getResources().getString(R.string.info_rosy_pelican);

            case "Himalayan Black Bear":
                return ctx.getResources().getString(R.string.info_himalayan_baloo);
            case "Lesser Flamingo":
                return ctx.getResources().getString(R.string.info_new_bird_section);

            case "Tiger":
                return ctx.getResources().getString(R.string.info_tigerhouse);
            case "Lion Tailed Monkey":
                return ctx.getResources().getString(R.string.info_liontailedmonkey);

            case "Sloth Bear":
                return ctx.getResources().getString(R.string.info_desibalu);
            case "Emu":
                return ctx.getResources().getString(R.string.info_emu);

            case "Hoolock Gibbon":
                return ctx.getResources().getString(R.string.info_hukku_bandar);

            case "Black Buck":
                return ctx.getResources().getString(R.string.info_blackbuck);
            case "Swamp Deer":
                return ctx.getResources().getString(R.string.info_barasingha);
            case "Hog Deer":
                return ctx.getResources().getString(R.string.info_padha);
            case "Leopard/ Panther":
                return ctx.getResources().getString(R.string.info_leopardhouse);
            case "Lion":
                return ctx.getResources().getString(R.string.info_vasundhara_mada_babar_sherni);
            case "(White Tiger)":
                return ctx.getResources().getString(R.string.info_safedbhag);
            case "Tigris":
                return ctx.getResources().getString(R.string.info_safedbhag);
        }
        if(tracker_name.contains("Tigris") || tracker_name.contains("tigris") || tracker_name.contains("Figris") || tracker_name.contains("figris") || tracker_name.contains("Whte"))
            return ctx.getResources().getString(R.string.info_safedbhag);
        if(tracker_name.contains("leopard") || tracker_name.contains("Panther"))
            return ctx.getResources().getString(R.string.info_leopardhouse);
        return "";
    }

    public static boolean OCRTagFound(String animal_name, Context ctx){

        switch (animal_name){

            case "Black Buck":
            return true;
        }
        return false;
    }

    public static String getTextfromFishTracker(String tracker_name, Context ctx){

        switch(tracker_name.split("_")[0]){
            case "1": return ctx.getResources().getString( R.string.info_arovana_fish);
            case "2": return ctx.getResources().getString( R.string.info_albino_red_tail_shark);
            case "3": return ctx.getResources().getString( R.string.info_grass_corp);
            case "4": return ctx.getResources().getString( R.string.info_albino_tiger_shark);
            case "5": return ctx.getResources().getString( R.string.info_algae_eater_fish);
            case "6": return ctx.getResources().getString( R.string.info_alligator_gar_fish);
            case "7": return ctx.getResources().getString( R.string.info_angel_fish);
            case "8": return ctx.getResources().getString( R.string.info_aretis_chiclet);
            case "9": return ctx.getResources().getString( R.string.info_asiatic_shark);
            case "10": return ctx.getResources().getString( R.string.info_big_head_fish);
            case "11": return ctx.getResources().getString( R.string.info_black_ghost_fish);
            case "12": return ctx.getResources().getString( R.string.info_black_more);
            case "13": return ctx.getResources().getString( R.string.info_black_tetra);
            case "14": return ctx.getResources().getString( R.string.info_blue_gorami);
            case "15": return ctx.getResources().getString( R.string.info_common_life_bearer); //check
            case "16": return ctx.getResources().getString( R.string.info_crocodile_fish);
            case "17": return ctx.getResources().getString( R.string.info_dyan_soni);
            case "18": return ctx.getResources().getString( R.string.info_gambiya_fish); //check
            case "19": return ctx.getResources().getString( R.string.info_yellow_gorami);
            case "20": return ctx.getResources().getString( R.string.info_wild_loach);
            case "21": return ctx.getResources().getString( R.string.info_whale_tail_angel);
            case "22": return ctx.getResources().getString( R.string.info_walking_cat_fish);
            case "23": return ctx.getResources().getString( R.string.info_venus_tetra_fish);
            case "24": return ctx.getResources().getString( R.string.info_tim_fall_barb_fish);
            case "25": return ctx.getResources().getString( R.string.info_tiger_shark);
            case "26": return ctx.getResources().getString( R.string.info_tiger_barb);
            case "27": return ctx.getResources().getString( R.string.info_sword_tailed_fish);
            case "28": return ctx.getResources().getString( R.string.info_skate_fish);
            case "29": return ctx.getResources().getString( R.string.info_albino_loach);
            case "30": return ctx.getResources().getString( R.string.info_silver_shark);
            case "31": return ctx.getResources().getString( R.string.info_silver_dollar);
            case "32": return ctx.getResources().getString( R.string.info_rohu_fish);
            case "33": return ctx.getResources().getString( R.string.info_suncat_fish);
            case "34": return ctx.getResources().getString( R.string.info_sarpitetra);
            case "35": return ctx.getResources().getString( R.string.info_katla_fish);
            case "36": return ctx.getResources().getString( R.string.info_sting_re);
            case "37": return ctx.getResources().getString( R.string.info_red_eye_tetra);
            case "38": return ctx.getResources().getString( R.string.info_red_common_gold);
            case "39": return ctx.getResources().getString( R.string.info_red_fin_chiclet);
            case "40": return ctx.getResources().getString( R.string.info_rainbow_shark);
            case "41": return ctx.getResources().getString( R.string.info_red_fin_tetra);
            case "42": return ctx.getResources().getString( R.string.info_cobra_gappi_fish);
            case "43": return ctx.getResources().getString( R.string.info_parrot_fish);
            case "44": return ctx.getResources().getString( R.string.info_pineapple_sword_tailed_fish);
            case "45": return ctx.getResources().getString( R.string.info_red_cap_fish);
            case "46": return ctx.getResources().getString( R.string.info_patra_knife_fish);
            case "47": return ctx.getResources().getString( R.string.info_kissing_gorami);
            case "48": return ctx.getResources().getString( R.string.info_lion_fish);
            case "49": return ctx.getResources().getString( R.string.info_milky_koi);
            case "50": return ctx.getResources().getString( R.string.info_nandes_fish);
            case "51": return ctx.getResources().getString( R.string.info_monoangel_fish);
            case "52": return ctx.getResources().getString( R.string.info_nan_fish);
            case "53": return ctx.getResources().getString( R.string.info_neontetra);
            case "54": return ctx.getResources().getString( R.string.info_silver_carp);
            case "55": return ctx.getResources().getString( R.string.info_calico_gold);
            case "56": return ctx.getResources().getString( R.string.info_convict_chiclet);
            case "57": return ctx.getResources().getString( R.string.info_koi_carp);
            case "58": return ctx.getResources().getString( R.string.info_red_cap_fish);
            case "59": return ctx.getResources().getString( R.string.info_rosy_barb);
            case "60": return ctx.getResources().getString( R.string.info_oscar_fish);
            case "61": return ctx.getResources().getString( R.string.info_common_carp);
            case "62": return ctx.getResources().getString( R.string.info_fire_mouth);
            case "63": return ctx.getResources().getString( R.string.info_flower_horn);
            case "64": return ctx.getResources().getString( R.string.info_zebra_denyu);
            case "65": return ctx.getResources().getString( R.string.info_common_carp_2);
            case "66": return ctx.getResources().getString( R.string.info_acuarium_brief_note);

        }
        return  "";
    }

   public static String reference_mac_ids[]= {"55:46:4F:E6:95:52",
            "55:46:4F:FB:7A:98",
            "55:46:4F:FB:7A:49",
            "55:46:4F:FB:7A:6A",
            "55:46:4F:FB:7A:59",
            "55:46:4F:FB:7A:9F",
            "55:46:4F:FB:7A:9A",
            "55:46:4F:FB:78:5A",
            "55:46:4F:FB:7A:5B",
            "55:46:4F:FB:7A:AB",
            "55:46:4F:FB:7A:9C",
            "55:46:4F:FB:78:71",
            "55:46:4F:FB:7A:8F",
            "55:46:4F:FB:7A:A7",
            "55:46:4F:FB:7A:62",
            "55:46:4F:FB:7A:61",
            "55:46:4F:FB:7A:AF",
            "55:46:4F:FB:7A:46",
            "55:46:4F:FB:7A:A6",
            "55:46:4F:FB:7A:AE",
            "55:46:4F:FB:7A:54",
            "55:46:4F:FB:7A:66",
            "55:46:4F:FB:7A:92",
            "55:46:4F:FB:7A:5E",
            "55:46:4F:FB:7A:8B",
            "55:46:4F:FB:7A:56",
            "55:46:4F:FB:7A:5C",
            "55:46:4F:FB:7A:52",
            "55:46:4F:FB:78:AF",
            "55:46:4F:FB:7A:95",
            "55:46:4F:FB:78:AL",
            "55:46:4F:E6:96:05",
            "55:46:4F:FB:7A:A2",
            "55:46:4F:FB:7A:50",
            "55:46:4F:FB:7A:9B",
            "55:46:4F:FB:78:66",
            "55:46:4F:FB:7A:AD",
            "55:46:4F:FB:7A:5F",
            "55:46:4F:FB:7A:4A",
            "55:46:4F:FB:7A:6E",
            "55:46:4F:FB:7A:96",
            "55:46:4F:FB:78:B2",
            "55:46:4F:FB:78:60",
            "55:46:4F:FB:7A:55",
            "55:46:4F:FB:7A:6D",
            "55:46:4F:FB:7A:53",
            "55:46:4F:FB:7A:A9",
            "55:46:4F:FB:7A:9D",
            "55:46:4F:FB:7A:4B",
            "55:46:4F:FB:7A:4D",
            "55:46:4F:FB:78:14",
            "55:46:4F:FB:7A:57",
            "55:46:4F:FB:7A:6C",
            "55:46:4F:FB:7A:A1",
            "55:46:4F:FB:7A:8C",
            "55:46:4F:FB:7A:A5",
            "55:46:4F:FB:7A:A4",
            "55:46:4F:FB:7A:8D",
            "55:46:4F:FB:78:B3",
            "55:46:4F:FB:78:72",
            "55:46:4F:FB:7A:5D",
            "55:46:4F:FB:78:63",
            "55:46:4F:FB:7A:91",
            "55:46:4F:FB:7A:AA",
            "55:46:4F:FB:7A:4E",
            "55:46:4F:FB:7A:6F",
            "55:46:4F:FB:78:65",
            "55:46:4F:FB:7A:51",
            "55:46:4F:FB:78:6F",
            "55:46:4F:FB:7A:AD",
            "55:46:4F:E6:93:AB",
            "55:46:4F:FB:7A:67",
            "55:46:4F:FB:78:0C",
            "55:46:4F:FB:78:13",
            "55:46:4F:FB:7A:93",
            "55:46:4F:FB:7A:90",
            "55:46:4F:FB:7A:8E",
            "55:46:4F:FB:7A:99",
            "55:46:4F:FB:7A:69",
            "55:46:4F:FB:78:B1",
            "55:46:4F:FB:78:B4",
            "55:46:4F:FB:7A:BO",
            "55:46:4F:FB:7A:A3",
            "55:46:4F:FB:7A:A6",
            "55:46:4F:FB:78:73",
            "55:46:4F:FB:7A:97",
            "55:46:4F:FB:78:AE",
            "55:46:4F:FB:7A:94",
            "55:46:4F:FB:78:6B",
            "55:46:4F:FB:7A:B5",
            "55:46:4F:E6:95:62",
            "55:46:4F:E6:95:4C",
            "55:46:4F:E6:93:AC",
            "55:46:4F:E6:93:72",
            "55:46:4F:FF:89:65"};

}
