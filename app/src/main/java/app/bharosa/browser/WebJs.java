package app.bharosa.browser;

import java.util.Locale;
import java.util.regex.Pattern;

/** JavaScript snippets injected into pages, and helpers for media links. */
public final class WebJs {
    private WebJs() { }

    /** Name of the JavaScript bridge object (see MainActivity.MediaBridge). */
    public static final String BRIDGE = "RangOneMedia";

    static final Pattern MEDIA_EXT = Pattern.compile(".*\\.(mp4|m4v|webm|mkv|mov|3gp|avi|flv|mp3|m4a|aac|ogg|oga|opus|wav|flac|m3u8|mpd)$");
    static final Pattern AUDIO_EXT = Pattern.compile(".*\\.(mp3|m4a|aac|ogg|oga|opus|wav|flac)$");
    static final Pattern SEGMENT_EXT = Pattern.compile(".*\\.(ts|m4s|key|vtt|srt)$");
    static final Pattern NON_MEDIA_EXT = Pattern.compile(".*\\.(js|css|json|png|jpe?g|gif|webp|svg|ico|woff2?|ttf|html?|php|pdf|xml|txt|wasm)$");

    /** Finds video / audio elements, &lt;source&gt;, media links, og:video and media network entries; reports each new URL. */
    public static final String MEDIA_SCAN =
            "(function(){" +
            "if(window.__rangMedia){try{window.__rangMedia()}catch(e){}return;}" +
            "var seen={};" +
            "var EXT=/\\.(mp4|m4v|webm|mkv|mov|3gp|mp3|m4a|aac|ogg|oga|opus|wav|flac|m3u8|mpd)(\\?|#|$)/i;" +
            "function abs(u){try{return new URL(u,location.href).href}catch(e){return u}}" +
            "function add(u,k,n){if(!u)return;u=String(u);if(u.indexOf('blob:')!=0)u=abs(u);" +
            "if(u.indexOf('data:')==0||u.indexOf('javascript:')==0)return;if(seen[u])return;seen[u]=1;" +
            "try{" + BRIDGE + ".report(u,k||'',n||'',document.title||'')}catch(e){}}" +
            "function scan(){try{" +
            "var els=document.querySelectorAll('video,audio');" +
            "for(var i=0;i<els.length;i++){var e=els[i];var k=e.tagName=='AUDIO'?'audio':'video';" +
            "add(e.currentSrc,k);add(e.getAttribute('src'),k);" +
            "var s=e.querySelectorAll('source');for(var j=0;j<s.length;j++)add(s[j].getAttribute('src'),k,s[j].getAttribute('label')||s[j].getAttribute('title')||'');" +
            "if(!e.__rangL){e.__rangL=1;e.addEventListener('loadedmetadata',scan);}}" +
            "var a=document.querySelectorAll('a[href]');for(var i=0;i<a.length&&i<3000;i++){var h=a[i].getAttribute('href');" +
            "if(h&&EXT.test(h.split('#')[0]))add(h,'',(a[i].textContent||'').replace(/\\s+/g,' ').trim().slice(0,80));}" +
            "var m=document.querySelectorAll('meta[property=\"og:video\"],meta[property=\"og:video:url\"],meta[property=\"og:video:secure_url\"],meta[property=\"og:audio\"],meta[name=\"twitter:player:stream\"]');" +
            "for(var i=0;i<m.length;i++){var c=m[i].getAttribute('content');if(c&&EXT.test(c))add(c,(m[i].getAttribute('property')||'').indexOf('audio')>0?'audio':'video');}" +
            "var r=performance.getEntriesByType('resource');for(var i=0;i<r.length;i++){if(EXT.test(r[i].name))add(r[i].name,'');}" +
            "}catch(e){}}" +
            "window.__rangMedia=scan;" +
            "document.addEventListener('play',scan,true);" +
            "document.addEventListener('loadeddata',scan,true);" +
            "try{document.addEventListener('touchstart',function(e){if(e.touches&&e.touches[0])window.__rangPt=[e.touches[0].clientX,e.touches[0].clientY]},{capture:true,passive:true});}catch(e){}" +
            "try{new MutationObserver(function(){clearTimeout(window.__rangT);window.__rangT=setTimeout(scan,800)})" +
            ".observe(document.documentElement,{childList:true,subtree:true,attributes:true,attributeFilter:['src']});}catch(e){}" +
            "var n=0;var iv=setInterval(function(){scan();if(++n>20)clearInterval(iv)},3000);" +
            "scan();" +
            "})();";

    /** Returns "video|src" / "audio|src" for the media element under the last touch point, or "". */
    public static final String TOUCHED_MEDIA =
            "(function(){var p=window.__rangPt;if(!p)return '';" +
            "var v=document.querySelectorAll('video,audio');" +
            "for(var i=0;i<v.length;i++){var r=v[i].getBoundingClientRect();" +
            "if(r.width>0&&r.height>0&&p[0]>=r.left&&p[0]<=r.right&&p[1]>=r.top&&p[1]<=r.bottom){var e=v[i];" +
            "var u=e.currentSrc||e.src||'';if(!u){var s=e.querySelector('source');if(s)u=s.src;}" +
            "return (e.tagName=='AUDIO'?'audio|':'video|')+u;}}" +
            "return '';})()";

    /** Extracts the main article text: {title, b:[[kind,text],...]} as a JSON string. */
    public static final String READER =
            "(function(){" +
            "function txt(e){return (e.innerText||e.textContent||'').replace(/\\s+/g,' ').trim();}" +
            "var root=document.querySelector('article')||document.querySelector('[role=main]')||document.querySelector('main');" +
            "if(!root||txt(root).length<500){var best=null,bs=0,score=new Map();var ps=document.querySelectorAll('p');" +
            "for(var i=0;i<ps.length;i++){var p=ps[i].parentElement;if(!p)continue;var l=txt(ps[i]).length;if(l<40)continue;" +
            "score.set(p,(score.get(p)||0)+l);var g=p.parentElement;if(g)score.set(g,(score.get(g)||0)+l/2);}" +
            "score.forEach(function(v,k){if(v>bs){bs=v;best=k;}});if(best)root=best;}" +
            "if(!root)root=document.body;" +
            "var out=[],n=0;var els=root.querySelectorAll('h1,h2,h3,h4,p,li,blockquote,pre,figcaption');" +
            "for(var i=0;i<els.length&&n<150000;i++){var e=els[i];" +
            "if(e.closest('nav,footer,aside,form,header,[aria-hidden=true],.share,.related,.comments'))continue;" +
            "if((e.tagName=='LI'||e.tagName=='BLOCKQUOTE')&&e.querySelector('p'))continue;" +
            "var t=txt(e);if(t.length<2)continue;if(e.tagName=='LI'&&t.length<25)continue;" +
            "var k=/^H/.test(e.tagName)?'h':(e.tagName=='LI'?'l':(e.tagName=='BLOCKQUOTE'?'q':(e.tagName=='FIGCAPTION'?'c':'p')));" +
            "out.push([k,t]);n+=t.length;}" +
            "if(n<200){var all=txt(root);if(all.length>n){out=[['p',all.slice(0,150000)]];}}" +
            "var h1=document.querySelector('h1');var ti=(h1?txt(h1):'')||document.title||'';" +
            "if(out.length&&out[0][0]=='h'&&out[0][1]==ti)out.shift();" +
            "return JSON.stringify({title:ti,b:out});" +
            "})()";

    private static final String NIGHT_CSS =
            "html{filter:invert(1) hue-rotate(180deg)!important;background:#fff!important}" +
            "img,video,picture,canvas,iframe,svg image,embed,object,[style*=\\\"background-image\\\"]{filter:invert(1) hue-rotate(180deg)!important}";

    /** Adds (on=true) or removes the night-mode style sheet. */
    public static String night(boolean on) {
        if (!on) return "(function(){var s=document.getElementById('__rangNight');if(s)s.remove();})();";
        return "(function(){if(document.getElementById('__rangNight'))return;var s=document.createElement('style');" +
                "s.id='__rangNight';s.textContent=\"" + NIGHT_CSS + "\";" +
                "(document.head||document.documentElement).appendChild(s);})();";
    }

    /** "video", "audio", "stream" (HLS / DASH playlist) or "blob". */
    public static String kindOf(String url, String hint) {
        if (url == null) return "video";
        if (url.startsWith("blob:")) return "blob";
        String p = path(url);
        if (p.endsWith(".m3u8") || p.endsWith(".mpd")) return "stream";
        if (AUDIO_EXT.matcher(p).matches()) return "audio";
        if ("audio".equals(hint)) return "audio";
        return "video";
    }

    /** Lower-case URL path without query / fragment. */
    public static String path(String url) {
        String u = url;
        int q = u.indexOf('?');
        if (q >= 0) u = u.substring(0, q);
        int h = u.indexOf('#');
        if (h >= 0) u = u.substring(0, h);
        return u.toLowerCase(Locale.ROOT);
    }

    public static boolean hasMediaExt(String url) { return MEDIA_EXT.matcher(path(url)).matches(); }

    public static String extOf(String url, String kind) {
        String p = path(url);
        int slash = p.lastIndexOf('/');
        int dot = p.lastIndexOf('.');
        if (dot > slash && dot >= 0 && p.length() - dot <= 5 && MEDIA_EXT.matcher(p).matches()) return p.substring(dot);
        return "audio".equals(kind) ? ".mp3" : ".mp4";
    }

    /** A safe file name made from the page title plus the media extension. */
    public static String fileName(String title, String url, String kind) {
        String ext = extOf(url, kind);
        String base = title == null ? "" : title.replaceAll("[\\\\/:*?\"<>|\\n\\r\\t]+", " ").replaceAll("\\s+", " ").trim();
        if (base.length() > 60) base = base.substring(0, 60).trim();
        if (base.isEmpty()) {
            String p = path(url);
            base = p.substring(p.lastIndexOf('/') + 1);
            int dot = base.lastIndexOf('.');
            if (dot > 0) base = base.substring(0, dot);
            if (base.isEmpty()) base = "video_" + System.currentTimeMillis();
        }
        return base + ext;
    }
}
