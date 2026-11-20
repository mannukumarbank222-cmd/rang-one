#!/bin/bash
# Real screenshots of Rang One on an Android 13 emulator (Pixel 2 profile, 1080x1920).
set -x
P=app.bharosa.browser
A=$P/.MainActivity
mkdir -p shots
shot() { sleep ${2:-3}; adb exec-out screencap -p > shots/$1.png; }
crashcheck() { adb logcat -d -b crash > shots/crash_$1.txt 2>&1; }
adb install -r RangOne_v4.7.apk
adb shell pm list packages | grep $P
adb shell dumpsys package $P | grep -E "versionName|versionCode|permission" > shots/dumpsys.txt
adb shell wm size; adb shell wm density
# clean status bar
adb shell settings put global sysui_demo_allowed 1
adb shell am broadcast -a com.android.systemui.demo -e command enter
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1030
adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 92 -e plugged false
adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 -e mobile show -e datatype 4g -e level 4
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false
# Hindi UI for the app
adb shell cmd locale set-app-locales $P --user 0 --locales hi-IN
sleep 1
adb shell am start -W -n $A
sleep 4
adb shell am start -W -n $A -a android.intent.action.VIEW -d "https://hi.wikipedia.org/wiki/%E0%A4%AD%E0%A4%BE%E0%A4%B0%E0%A4%A4"
sleep 12
adb shell am start -W -n $A -a android.intent.action.VIEW -d "https://www.india.gov.in/"
sleep 12
adb shell am start -W -n $A -a app.bharosa.browser.NEW_PRIVATE_TAB
sleep 3
adb shell input text "hi.wikipedia.org"
adb shell input keyevent 66
sleep 12
crashcheck 1
adb shell am start -W -n $A -a app.bharosa.browser.SHOW_TABS
shot 03_tabs 4
adb shell input keyevent 4
sleep 1
adb shell am start -W -n $A -a android.intent.action.VIEW -d "https://hi.wikipedia.org/wiki/%E0%A4%AD%E0%A4%BE%E0%A4%B0%E0%A4%A4"
sleep 12
adb shell input keyevent 82
shot 02_page_menu 3
adb shell input keyevent 4
sleep 1
shot extra_page 1
adb shell am start -W -n $A -a app.bharosa.browser.HISTORY
shot 04_history 3
adb shell input keyevent 4
crashcheck 2
# fresh start -> native home with recent history
adb shell am force-stop $P
sleep 1
adb shell am start -W -n $A
shot 01_home 5
# English home
adb shell cmd locale set-app-locales $P --user 0 --locales en-US
sleep 2
adb shell am force-stop $P
adb shell am start -W -n $A
shot extra_home_en 5
adb shell input keyevent 82
shot extra_menu_en 3
adb shell input keyevent 4
adb shell am start -W -n $A -a app.bharosa.browser.NEW_PRIVATE_TAB
sleep 2
adb shell input keyevent 4
shot extra_private_home 2
adb shell cmd locale set-app-locales $P --user 0 --locales hi-IN
sleep 2
adb shell am force-stop $P
adb shell am start -W -n $A
sleep 3
adb shell input keyevent 82
sleep 2
# tap About in the menu is coordinate-dependent; open Settings/About through intents is not exported, so use UI dump
adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb pull /sdcard/ui.xml shots/ui_menu.xml
python3 - <<'P'
import re,subprocess
s=open('shots/ui_menu.xml',encoding='utf8').read()
def tap(label):
    m=re.search(r'text="%s"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'%re.escape(label),s)
    if m:
        x=(int(m.group(1))+int(m.group(3)))//2; y=(int(m.group(2))+int(m.group(4)))//2
        subprocess.call(['adb','shell','input','tap',str(x),str(y)]); return True
    return False
print('about tapped', tap('ऐप के बारे में') or tap('About'))
P
shot extra_about 3
adb shell input keyevent 4
sleep 1
adb shell input keyevent 82
sleep 2
adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb pull /sdcard/ui.xml shots/ui_menu2.xml
python3 - <<'P'
import re,subprocess
s=open('shots/ui_menu2.xml',encoding='utf8').read()
m=re.search(r'text="(सेटिंग्स|Settings)"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"',s)
if m:
    x=(int(m.group(2))+int(m.group(4)))//2; y=(int(m.group(3))+int(m.group(5)))//2
    subprocess.call(['adb','shell','input','tap',str(x),str(y)])
P
shot extra_settings 3
crashcheck 3
adb logcat -d > shots/logcat.txt 2>&1
ls -la shots
exit 0
