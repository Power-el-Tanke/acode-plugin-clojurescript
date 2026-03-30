(ns plugin-name.main
    (:require ["node:fs/promises" :refer (readFile close)]))

(def plugin
    (-> (readFile "../plugin.json")
          (.then #(js->clj (.parse js/JSON %)))
          (.catch println)
          (.finally close)))

(defn init
    []
    (do (comment plugin initialization)))

(defn destroy []
    (do (comment plugin clean up)))


(defn main []
    (when (window/acode)
          (let [acode-plugin (new AcodePlugin)
                plugin-id (:id plugin)]
               (letfn [(plugin-init 
                        [base-url 
                         page 
                         plugin-file]
                        (do (set! (.-baseUrl acode-plugin)
                                  (if (.endsWith "/" base-url)
                                      (base-url)
                                      (str base-url "/")))
                            (-> (.init acode-plugin page
                                                    (get plugin-file cacheFile)
                                                    (get plugin-file cacheFileUrl))
                                (.then identity))))]
                      (do
                         (acode/setPluginInit plugin-id plugin-init)
                         (acode/setPluginUnmount plugin-id #(.destroy acode-plugin)))))))