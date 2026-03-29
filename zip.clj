(ns zip
    (:require [org.clojure.data.json :as [json]])
    (:require [clojure.java.io :as [io]])
    (:import [java.util.zip ZipOutputStream ZipEntry]
             [java.io FileInputStream FileOutputStream]))

(defn file->zip
    [path zip-writter dest]
    (with-open [in (io/input-stream dest)]
               (zip in zip-writter dest))

(defn dir->zip
    [file zip-writter dest]
    (if (.isDirectory file)
        (doseq [f (.listFiles file)]
               (dir->file (f zip-writter (str dest 
                                              (.getName f) 
                                              "/"))))
        (file->zip f zip-writter (str dest 
                                      (.getName f))))
                
(defn zip
    [in zip-writter dest]
    (do (.putNextEntry zip-writter (ZipEntry. dest))
        (io/copy in zip-writter)
        (.closeEntry zip-writter)))

(let [plugin-json (json/read (slurp "plugin.json"))
     changelog (:changelogs plugin-json)
     readme (:readme plugin-json)]
     (do (with-open [os (io/output-stream "plugin.zip")
                     zip-writter (ZipOutputStream. os)]
                    (path->zip "icon.png" zip-writter "icon.png")
                    (zip plugin-json zip-writter "plugin.json")
                    (when changelog
                          (path->zip changelog zip-writter "CHANGELOG.md"))
                    (when readme
                          (path->zip readme zip-writter "README.md"))
                    with-open [dist (io/file "output/")]
                              (dir->zip dist zipper-writter "dist/"))
         (println "plugin.zip written")))