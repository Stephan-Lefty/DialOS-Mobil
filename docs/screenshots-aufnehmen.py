#!/usr/bin/env python3
"""Screenshots für den Play Store vom angeschlossenen Gerät holen."""
import re
import subprocess
import sys
import time

ADB = "/home/stephan/Android/Sdk/platform-tools/adb"
PKG = "org.dialos.mobil.debug"
OUT = "/mnt/raid/eigene Daten/GitHub/Stephan-Lefty/DialOS-Mobil/screenshots"


def sh(*args, binary=False):
    r = subprocess.run([ADB, *args], capture_output=True)
    return r.stdout if binary else r.stdout.decode("utf-8", "replace")


def bounds(view_id):
    sh("shell", "uiautomator", "dump", "/sdcard/u.xml")
    xml = sh("shell", "cat", "/sdcard/u.xml")
    for node in xml.split("<"):
        if view_id in node:
            m = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', node)
            if m:
                x1, y1, x2, y2 = map(int, m.groups())
                return (x1 + x2) // 2, (y1 + y2) // 2
    return None


def tap(view_id):
    pos = bounds(view_id)
    if not pos:
        print(f"  ! {view_id} nicht gefunden")
        return False
    sh("shell", "input", "tap", str(pos[0]), str(pos[1]))
    return True


def shot(name):
    data = sh("exec-out", "screencap", "-p", binary=True)
    path = f"{OUT}/{name}.png"
    with open(path, "wb") as f:
        f.write(data)
    print(f"  {name}.png ({len(data)//1024} kB)")


sh("shell", "svc", "power", "stayon", "usb")
sh("shell", "am", "force-stop", PKG)
time.sleep(1)
sh("shell", "am", "start", "-n", f"{PKG}/org.dialos.mobil.MainActivity")
time.sleep(3)

print("1) Startseite, ausgeschaltet")
shot("01_startseite_aus")

print("2) Sprachsteuerung einschalten")
if tap("btnToggle"):
    time.sleep(14)
    shot("02_startseite_hoert_zu")

print("3) Infos & Einstellungen")
if tap("btnInfo"):
    time.sleep(2)
    shot("03_infos_einstellungen")
    sh("shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(2)

print("4) Kontrastansicht")
if tap("btnContrast"):
    time.sleep(3)
    shot("04_kontrastansicht")
    tap("btnContrast")
    time.sleep(2)

print("5) wieder ausschalten")
tap("btnToggle")
sh("shell", "svc", "power", "stayon", "false")
print("fertig")
