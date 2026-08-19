package org.dialos.mobil

import android.app.Application

/**
 * Bewusst ohne Material-You-Farben (`DynamicColors`): die App soll das
 * DialOS-Blau tragen, und der Kontrast der Schaltflächen darf nicht vom
 * Hintergrundbild des Nutzers abhängen. Bei einer App für sehbehinderte
 * Menschen ist ein festes, geprüftes Farbschema mehr wert als ein
 * mitwachsendes.
 */
class DialOsMobilApp : Application()
