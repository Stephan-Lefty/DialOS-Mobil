#!/usr/bin/env python3
"""Gemeinsamer Zugang zur WordPress-Schnittstelle von dialos.org.

Die Zugangsdaten stehen NICHT im Repo. Sie werden in dieser Reihenfolge
gesucht, die erste vorhandene Datei gewinnt:

  1. Pfad aus der Umgebungsvariablen DIALOS_WP_ENV
  2. website/.env  (neben diesem Skript, steht in .gitignore)
  3. ~/.config/dialos/wordpress.env
  4. DialOS/Wordpressinstallation/.env  - dort liegt die Datei bislang.

Punkt 4 ist der Stand vom 20.08.2026 und der Grund, warum die Suche
mehrstufig ist: Die WordPress-Werkzeuge sind urspruenglich im Repo DialOS
entstanden und am 20.08.2026 hierher gezogen, damit die beiden Projekte
getrennt bleiben. Die Zugangsdatei ist mit umgezogen worden oder eben
nicht - beides funktioniert.

Aufbau der Datei (drei Zeilen):

    WP_URL=https://dialos.org
    WP_USER=ClaudIA
    WP_APP_PASSWORD=xxxx xxxx xxxx xxxx xxxx xxxx

Das Passwort ist ein Anwendungspasswort aus dem WordPress-Profil, kein
Anmeldepasswort. Es laesst sich dort jederzeit einzeln zurueckziehen.
"""
import base64
import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path

HIER = Path(__file__).resolve().parent

KANDIDATEN = [
    Path(os.environ["DIALOS_WP_ENV"]) if os.environ.get("DIALOS_WP_ENV") else None,
    HIER / ".env",
    Path.home() / ".config" / "dialos" / "wordpress.env",
    HIER.parent.parent / "DialOS" / "Wordpressinstallation" / ".env",
]


def env_datei():
    """Erste vorhandene Zugangsdatei - oder ein verstaendlicher Abbruch."""
    for pfad in KANDIDATEN:
        if pfad and pfad.is_file():
            return pfad

    gesucht = "\n  ".join(str(p) for p in KANDIDATEN if p)
    sys.exit(
        "Keine Zugangsdaten fuer dialos.org gefunden. Gesucht wurde in:\n  "
        + gesucht
        + "\n\nAnlegen (Aufbau siehe Kopf von wp_zugang.py) oder den Pfad "
        "ueber DIALOS_WP_ENV setzen."
    )


def lade_zugang():
    cfg = {}
    for zeile in env_datei().read_text().splitlines():
        zeile = zeile.strip()
        if not zeile or zeile.startswith("#") or "=" not in zeile:
            continue
        schluessel, wert = zeile.split("=", 1)
        cfg[schluessel.strip()] = wert.strip().strip('"').strip("'")

    fehlend = [k for k in ("WP_URL", "WP_USER", "WP_APP_PASSWORD")
               if not cfg.get(k)]
    if fehlend:
        sys.exit(f"In der Zugangsdatei fehlt: {', '.join(fehlend)}")

    return cfg


CFG = lade_zugang()
WP = CFG["WP_URL"].rstrip("/")
AUTH = base64.b64encode(
    f"{CFG['WP_USER']}:{CFG['WP_APP_PASSWORD']}".encode()
).decode()


def call(method, route, payload=None, still=False):
    """Ein Aufruf gegen /wp-json/<route>.

    still=True gibt bei einem Fehler None zurueck statt abzubrechen -
    gedacht fuer Abfragen, die auch fehlschlagen duerfen.
    """
    data = json.dumps(payload).encode() if payload is not None else None
    req = urllib.request.Request(f"{WP}/wp-json/{route}", data=data,
                                 method=method)
    req.add_header("Authorization", f"Basic {AUTH}")
    req.add_header("Content-Type", "application/json")

    try:
        with urllib.request.urlopen(req, timeout=90) as antwort:
            return json.loads(antwort.read().decode())
    except urllib.error.HTTPError as fehler:
        text = fehler.read().decode()[:400]
        print(f"FEHLER {fehler.code} bei {method} {route}:\n{text}",
              file=sys.stderr)
        if still:
            return None
        raise
