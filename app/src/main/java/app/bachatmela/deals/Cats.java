package app.bachatmela.deals;

/** Category keys used in deals.json, with Hindi + English labels and a colour. */
public final class Cats {
    public static final String LOOT = "loot";
    public static final String[] KEYS = {"fashion", "electronics", "home", "beauty", "books_toys", LOOT};
    public static final String[] HI = {"कपड़े", "मोबाइल और इलेक्ट्रॉनिक्स", "घर और किचन", "ब्यूटी", "किताबें और खिलौने", "लूट डील्स 70%+"};
    public static final String[] EN = {"Fashion", "Mobile & Electronics", "Home & Kitchen", "Beauty", "Books & Toys", "Loot Deals 70%+ off"};
    public static final int[] COLOR = {0xFFFF7A00, 0xFF2D6CDF, 0xFF1E9E4A, 0xFFD6336C, 0xFF7B4BD6, 0xFFE03131};
    public static final String[] STORES = {"Amazon", "Flipkart", "Meesho", "Myntra", "Ajio"};

    public static int index(String key) {
        for (int i = 0; i < KEYS.length; i++) if (KEYS[i].equals(key)) return i;
        return -1;
    }
    public static String hi(String key) { int i = index(key); return i < 0 ? key : HI[i]; }
    public static String en(String key) { int i = index(key); return i < 0 ? key : EN[i]; }
    public static int color(String key) { int i = index(key); return i < 0 ? 0xFF888888 : COLOR[i]; }

    public static int storeColor(String store) {
        if (store == null) return 0xFF666666;
        switch (store.toLowerCase()) {
            case "amazon": return 0xFF232F3E;
            case "flipkart": return 0xFF2874F0;
            case "meesho": return 0xFF9F2089;
            case "myntra": return 0xFFE2366F;
            case "ajio": return 0xFF2C4152;
            default: return 0xFF666666;
        }
    }
    private Cats() { }
}
