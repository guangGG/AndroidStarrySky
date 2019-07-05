package gapp.season.star.bsc5;

public enum GreekLetter {
    x1("α", "alpha"),
    x2("β", "beta"),
    x3("γ", "gamma"),
    x4("δ", "delta"),
    x5("ε", "epsilon"),
    x6("ζ", "zeta"),
    x7("η", "eta"),
    x8("θ", "theta"),
    x9("ι", "iota"),
    x10("κ", "kappa"),
    x11("λ", "lambda"),
    x12("μ", "mu"),
    x13("ν", "nu"),
    x14("ξ", "xi"),
    x15("ο", "omicron"),
    x16("π", "pi"),
    x17("ρ", "rho"),
    x18("σ", "sigma"), //σ,ς
    x19("τ", "tau"),
    x20("υ", "upsilon"),
    x21("φ", "phi"),
    x22("χ", "chi"),
    x23("ψ", "psi"),
    x24("ω", "omega");


    private String nameLetter;
    private String nameEn;

    GreekLetter(String nameLetter, String nameEn) {
        this.nameLetter = nameLetter;
        this.nameEn = nameEn;
    }

    public static String getGreekLetter(String starName) {
        if (starName != null) {
            String num = starName.substring(0, 3).trim(); //前3位为星座内数字编号
            String no = starName.substring(6, 7).trim(); //第7位为多星编号
            starName = ((starName.length() == 10) ? starName.substring(3, 6) : starName).toLowerCase(); //第4-6位为希腊字母缩写
            for (GreekLetter letter : values()) {
                //星表正常字母缩写为前3位(部分缩写为2位)
                String nameEn = letter.nameEn;
                String nameShort = nameEn.length() > 2 ? nameEn.substring(0, 3) : nameEn;
                if (starName.contains(nameShort)) {
                    return letter.nameLetter + no;
                }
            }
            return num;
        }
        return "";
    }
}
