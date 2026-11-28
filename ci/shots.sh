#!/bin/bash
# Screenshots of Bachat Mela on an Android 13 emulator (Pixel 2 profile, 1080x1920).
set -x
P=app.bachatmela.deals
A=$P/.MainActivity
mkdir -p shots
shot() { sleep ${2:-3}; adb exec-out screencap -p > shots/$1.png; }
crashcheck() { adb logcat -d -b crash > shots/crash_$1.txt 2>&1; }
go() { adb shell am start -W -n $A "$@"; }
tap() {  # tap the first view whose text contains $1
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb pull /sdcard/ui.xml shots/ui_last.xml >/dev/null 2>&1
  python3 - "$1" <<'P'
import re,subprocess,sys
s=open('shots/ui_last.xml',encoding='utf8').read()
lab=sys.argv[1]
for m in re.finditer(r'<node [^>]*?text="([^"]*)"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"',s):
    if lab in m.group(1):
        x=(int(m.group(2))+int(m.group(4)))//2; y=(int(m.group(3))+int(m.group(5)))//2
        subprocess.call(['adb','shell','input','tap',str(x),str(y)]); print('tapped',lab,x,y); sys.exit(0)
print('NOT FOUND',lab)
P
}
adb install -r BachatMela_v1.0.apk
adb shell pm list packages | grep $P
adb shell dumpsys package $P | grep -E "versionName|versionCode|permission" > shots/dumpsys.txt
adb shell wm size; adb shell wm density
adb shell settings put global sysui_demo_allowed 1
adb shell am broadcast -a com.android.systemui.demo -e command enter
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1030
adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 92 -e plugged false
adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 -e mobile show -e datatype 4g -e level 4
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false
# warm up: first launch fetches remote deals and caches images
go; sleep 20
adb shell am force-stop $P; sleep 1
go
shot 01_home 10
crashcheck 1
go --es tab home --es cat electronics --es store all
shot 02_category_electronics 6
go --es tab home --es cat loot --es store all
shot extra_category_loot 5
go --es tab cats
shot 07_categories 4
# deal detail + save some favourites
go --es tab home --es cat all --es deal bm006
shot 03_detail 6
tap "सेव करें"; sleep 2
shot extra_detail_saved 1
go --es deal bm003; sleep 4; tap "सेव करें"; sleep 2
go --es deal bm016; sleep 4; tap "सेव करें"; sleep 2
crashcheck 2
go --es tab favs --es recent 0
shot 04_favourites 5
go --es tab favs --es recent 1
shot 08_recently_viewed 4
go --es tab calc
shot 05_calculator 4
go --es tab about
shot 06_about 4
go --es tab home --es cat all --es store Flipkart --es list 1
shot 09_list_flipkart 6
go --es tab home --es cat all --es store all --es list 0 --es q earbuds
shot 10_search 5
crashcheck 3
# Buy button must hand off to an external browser, not an in-app WebView
go --es tab home --es deal bm007; sleep 4
tap "Buy on"; sleep 6
adb shell dumpsys activity activities | grep -E "topResumedActivity|mResumedActivity" > shots/buy_handoff.txt
shot extra_buy_handoff 3
adb shell input keyevent 4; sleep 1
# offline behaviour
adb shell svc wifi disable; adb shell svc data disable; sleep 3
adb shell am force-stop $P; sleep 1
go
shot extra_offline 6
adb shell svc wifi enable; adb shell svc data enable
crashcheck 4
adb logcat -d > shots/logcat.txt 2>&1
ls -la shots
exit 0
