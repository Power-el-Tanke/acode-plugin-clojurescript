(ns dev.zip
    (:require [clojure.data.json :as json])
    (:require [clojure.java.io :as io])
    (:import [java.util.zip ZipOutputStream ZipEntry]))

(defn zip-object
    [in zip-writter dest]
    (do (.putNextEntry zip-writter (ZipEntry. dest))
        (io/copy in zip-writter)
        (.closeEntry zip-writter)))

(defn file->zip
    [file zip-writter dest]
    (with-open [in (io/input-stream file)]
               (zip-object in zip-writter dest)))

(defn dir->zip
    [file zip-writter dest]
    (if (.isDirectory file)
        (do (.putNextEntry zip-writter (ZipEntry. (str dest "/")))
            (doseq [f (.listFiles file)]
               (dir->zip f zip-writter (str dest 
                                            "/"
                                            (.getName f)))))
        
        (file->zip file zip-writter dest)))
                
(defn zip-project 
    {:shadow.build/stage :flush}
    [build-state]
    (let [plugin-json (slurp "plugin.json")
          plugin (json/read-str plugin-json)
          changelog (:changelogs plugin)
          readme (:readme plugin)]
         (do (with-open [os (io/output-stream "plugin.zip")
                         zip-writter (ZipOutputStream. os)]
                        (file->zip "icon.png" zip-writter "icon.png")
                        (zip-object plugin-json zip-writter "plugin.json")
                        (when changelog
                              (file->zip changelog zip-writter "CHANGELOG.md"))
                        (when readme
                              (file->zip readme zip-writter "README.md"))
                        (dir->zip (io/file "output/") zip-writter "dist")
             (println "plugin.zip written")
             build-state))))