import i18n from "i18next"
import { initReactI18next } from "react-i18next"

import locales from "./locales"

i18n.use(initReactI18next).init({
  resources: locales,
  lng: "en",
  fallbackLng: "en",
  whitelist: ["en"],
  interpolation: {
    escapeValue: false // not needed for react as it escapes by default
  }
})

export default i18n
