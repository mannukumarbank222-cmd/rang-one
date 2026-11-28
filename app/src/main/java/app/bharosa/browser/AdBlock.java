package app.bharosa.browser;

import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Locale;

/** Built-in ad and tracker blocker: a small list of well-known ad / tracking hosts, matched with all parent domains. */
public final class AdBlock {
    /** Mirrors Prefs.adBlock; read from the WebView network thread. */
    public static volatile boolean enabled = true;

    private static final HashSet<String> HOSTS = new HashSet<>();

    static {
        String[] h = {
                // Google ads / analytics
                "doubleclick.net", "googlesyndication.com", "googleadservices.com", "google-analytics.com",
                "googletagmanager.com", "googletagservices.com", "adservice.google.com", "adservice.google.co.in",
                "pagead2.googlesyndication.com", "partner.googleadservices.com",
                // Ad exchanges / SSPs / DSPs
                "adnxs.com", "adsrvr.org", "pubmatic.com", "rubiconproject.com", "openx.net", "casalemedia.com",
                "criteo.com", "criteo.net", "amazon-adsystem.com", "advertising.com", "smartadserver.com", "adform.net",
                "media.net", "contextweb.com", "sharethrough.com", "teads.tv", "spotxchange.com", "springserve.com",
                "33across.com", "bidswitch.net", "lijit.com", "sovrn.com", "indexww.com", "gumgum.com", "yieldmo.com",
                "adroll.com", "zedo.com", "serving-sys.com", "adsafeprotected.com", "doubleverify.com", "moatads.com",
                "adcolony.com", "applovin.com", "inmobi.com", "unityads.unity3d.com", "vungle.com", "chartboost.com",
                "mopub.com", "startapp.com", "smaato.net", "adtech.de", "yieldlab.net", "districtm.io", "triplelift.com",
                "3lift.com", "onetag-sys.com", "emxdgt.com", "adkernel.com", "rhythmone.com", "undertone.com",
                // Native / content-recommendation ads
                "taboola.com", "outbrain.com", "mgid.com", "revcontent.com", "zergnet.com", "adskeeper.co.uk", "adskeeper.com",
                "vdo.ai", "clmbtech.com", "colombiaonline.com", "izooto.com",
                // Pop-under / aggressive networks
                "popads.net", "popcash.net", "propellerads.com", "propellerclick.com", "exoclick.com", "exosrv.com",
                "juicyads.com", "trafficjunky.net", "hilltopads.net", "adsterra.com", "adsterratech.com", "admaven.com",
                "ad-maven.com", "clickadu.com", "onclickads.net", "onclkds.com", "richpush.co", "pushnami.com",
                "popunder.net", "monetag.com", "a-ads.com", "coinzilla.com", "bidvertiser.com",
                // Trackers / analytics
                "scorecardresearch.com", "quantserve.com", "quantcount.com", "chartbeat.com", "chartbeat.net",
                "hotjar.com", "mixpanel.com", "bluekai.com", "krxd.net", "demdex.net", "everesttech.net", "rlcdn.com",
                "adsymptotic.com", "agkn.com", "bat.bing.com", "clarity.ms", "mc.yandex.ru", "an.yandex.ru", "adfox.ru",
                "ads.yahoo.com", "analytics.yahoo.com", "ads.twitter.com", "static.ads-twitter.com", "ads.linkedin.com",
                "px.ads.linkedin.com", "app-measurement.com", "appsflyer.com", "adjust.com", "kochava.com",
                "newrelic.com", "nr-data.net", "comscore.com", "mathtag.com", "tapad.com", "crwdcntrl.net", "exelator.com",
                "eyeota.net", "omnitagjs.com", "zemanta.com", "tremorhub.com", "ipredictive.com", "liadm.com",
        };
        for (String s : h) HOSTS.add(s);
    }

    private AdBlock() { }

    /** True when the host or any of its parent domains is on the list. */
    public static boolean isAd(String host) {
        if (host == null || host.isEmpty()) return false;
        String h = host.toLowerCase(Locale.ROOT);
        while (true) {
            if (HOSTS.contains(h)) return true;
            int dot = h.indexOf('.');
            if (dot < 0 || dot == h.length() - 1) return false;
            h = h.substring(dot + 1);
            if (h.indexOf('.') < 0) return false; // stop at the TLD
        }
    }

    /** Empty response returned instead of a blocked request. */
    public static WebResourceResponse empty() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(new byte[0]));
    }
}
