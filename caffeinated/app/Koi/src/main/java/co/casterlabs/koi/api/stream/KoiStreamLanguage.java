package co.casterlabs.koi.api.stream;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KoiStreamLanguage {
    AB("Abkhazian"),
    AA("Afar"),
    AF("Afrikaans"),
    SQ("Albanian"),
    AM("Amharic"),
    AR("Arabic"),
    HY("Armenian"),
    AS("Assamese"),
    AY("Aymara"),
    AZ("Azerbaijani"),
    BA("Bashkir"),
    EU("Basque"),
    BN("Bengali"),
    DZ("Bhutani"),
    BH("Bihari"),
    BI("Bislama"),
    BR("Breton"),
    BG("Bulgarian"),
    MY("Burmese"),
    BE("Byelorussian"),
    KM("Cambodian"),
    CA("Catalan"),
    ZH("Chinese"),
    CO("Corsican"),
    HR("Croatian"),
    CS("Czech"),
    DA("Danish"),
    NL("Dutch"),
    EN("English"),
    EO("Esperanto"),
    ET("Estonian"),
    FO("Faeroese"),
    FJ("Fiji"),
    FI("Finnish"),
    FR("French"),
    FY("Frisian"),
    GD("Gaelic (Scots Gaelic)"),
    GL("Galician"),
    KA("Georgian"),
    DE("German"),
    EL("Greek"),
    KL("Greenlandic"),
    GN("Guarani"),
    GU("Gujarati"),
    HA("Hausa"),
    IW("Hebrew"),
    HI("Hindi"),
    HU("Hungarian"),
    IS("Icelandic"),
    IN("Indonesian"),
    IA("Interlingua"),
    IE("Interlingue"),
    IK("Inupiak"),
    GA("Irish"),
    IT("Italian"),
    JA("Japanese"),
    JW("Javanese"),
    KN("Kannada"),
    KS("Kashmiri"),
    KK("Kazakh"),
    RW("Kinyarwanda"),
    KY("Kirghiz"),
    RN("Kirundi"),
    KO("Korean"),
    KU("Kurdish"),
    LO("Laothian"),
    LA("Latin"),
    LV("Latvian"),
    LN("Lingala"),
    LT("Lithuanian"),
    MK("Macedonian"),
    MG("Malagasy"),
    MS("Malay"),
    ML("Malayalam"),
    MT("Maltese"),
    MI("Maori"),
    MR("Marathi"),
    MO("Moldavian"),
    MN("Mongolian"),
    NA("Nauru"),
    NE("Nepali"),
    NO("Norwegian"),
    OC("Occitan"),
    OR("Oriya"),
    OM("Oromo"),
    PS("Pashto"),
    FA("Persian"),
    PL("Polish"),
    PT("Portuguese"),
    PA("Punjabi"),
    QU("Quechua"),
    RM("Rhaeto-Romance"),
    RO("Romanian"),
    RU("Russian"),
    SM("Samoan"),
    SG("Sangro"),
    SA("Sanskrit"),
    SR("Serbian"),
    SH("Serbo-Croatian"),
    ST("Sesotho"),
    TN("Setswana"),
    SN("Shona"),
    SD("Sindhi"),
    SI("Singhalese"),
    SS("Siswati"),
    SK("Slovak"),
    SL("Slovenian"),
    SO("Somali"),
    ES("Spanish"),
    SU("Sudanese"),
    SW("Swahili"),
    SV("Swedish"),
    TL("Tagalog"),
    TG("Tajik"),
    TA("Tamil"),
    TT("Tatar"),
    TE("Tegulu"),
    TH("Thai"),
    BO("Tibetan"),
    TI("Tigrinya"),
    TO("Tonga"),
    TS("Tsonga"),
    TR("Turkish"),
    TK("Turkmen"),
    TW("Twi"),
    UK("Ukrainian"),
    UR("Urdu"),
    UZ("Uzbek"),
    VI("Vietnamese"),
    VO("Volapuk"),
    CY("Welsh"),
    WO("Wolof"),
    XH("Xhosa"),
    JI("Yiddish"),
    YO("Yoruba"),
    ZU("Zulu"),

    OTHER("Other");

    public static final Map<KoiStreamLanguage, String> LANG;

    static {
        Map<KoiStreamLanguage, String> map = new HashMap<>();
        LANG = Collections.unmodifiableMap(map);

        for (KoiStreamLanguage l : values()) {
            map.put(l, l.lang);
        }
    }

    private String lang;

    public static JsonObject toJson() {
        JsonObject json = new JsonObject();

        for (KoiStreamLanguage rating : values()) {
            json.put(rating.name(), rating.lang);
        }

        return json;
    }

}
