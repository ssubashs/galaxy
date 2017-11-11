/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module meta-data-service-js/meta_data_service */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JMetaDataService = Java.type('com.fun.ms.metadata.service.MetaDataService');
var MetaDataResponse = Java.type('com.fun.ms.common.service.MetaDataResponse');
var MetaDataRequest = Java.type('com.fun.ms.common.service.MetaDataRequest');

/**
 @class
*/
var MetaDataService = function(j_val) {

  var j_metaDataService = j_val;
  var that = this;

  /**

   @public
   @param request {Object} 
   @param resultHandler {function} 
   */
  this.process = function(request, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_metaDataService["process(com.fun.ms.common.service.MetaDataRequest,io.vertx.core.Handler)"](request != null ? new MetaDataRequest(new JsonObject(Java.asJSONCompatible(request))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_metaDataService;
};

MetaDataService._jclass = utils.getJavaClass("com.fun.ms.metadata.service.MetaDataService");
MetaDataService._jtype = {
  accept: function(obj) {
    return MetaDataService._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(MetaDataService.prototype, {});
    MetaDataService.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
MetaDataService._create = function(jdel) {
  var obj = Object.create(MetaDataService.prototype, {});
  MetaDataService.apply(obj, arguments);
  return obj;
}
/**

 @memberof module:meta-data-service-js/meta_data_service
 @param vertx {Vertx} 
 @return {MetaDataService}
 */
MetaDataService.create = function(vertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(MetaDataService, JMetaDataService["create(io.vertx.core.Vertx)"](vertx._jdel));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = MetaDataService;