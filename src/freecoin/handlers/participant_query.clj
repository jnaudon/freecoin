;; Freecoin - digital social currency toolkit

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2015 Dyne.org foundation
;; Copyright (C) 2015 Thoughtworks, Inc.

;; Sourcecode designed, written and maintained by
;; Denis Roio <jaromil@dyne.org>

;; With contributions by
;; Duncan Mortimer <dmortime@thoughtworks.com>

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns freecoin.handlers.participant-query
  (:require [liberator.core :as lc]
            [freecoin.utils :as utils]
            [freecoin.db.wallet :as wallet]
            [freecoin.context-helpers :as ch]
            [freecoin.views :as fv]
            [freecoin.views.participants-query-form :as participants-query-form]
            [freecoin.views.participants-list :as participants-list]))

(lc/defresource query-form [wallet-store]
  :allowed-methods [:get]
  :available-media-types ["text/html"]
  :authorized? (fn [ctx]
                 (when-let [uid (ch/context->signed-in-uid ctx)]
                   (when (wallet/fetch wallet-store uid) true)))
  :handle-ok (fn [ctx]
               (-> {}
                   participants-query-form/build
                   fv/render-page)))

(defn request->wallet-query [request]
  (if-let [{:keys [field value]} (-> request :params
                                     (utils/select-all-or-nothing [:field :value]))]
    {(keyword field) value}
    {}))

(lc/defresource participants [wallet-store]
  :allowed-methods [:get]
  :available-media-types ["text/html"]
  :authorized? (fn [ctx]
                 (when-let [uid (ch/context->signed-in-uid ctx)]
                   (when (wallet/fetch wallet-store uid) true)))
  :exists? (fn [ctx]
             {::wallets (->> ctx :request
                             request->wallet-query
                             (wallet/query wallet-store))})
  :handle-ok (fn [ctx]
               (-> {:wallets (::wallets ctx)}
                   participants-list/participants-list
                   fv/render-page)))

