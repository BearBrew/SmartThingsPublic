/**
 *  Roomba
 *
 *  Copyright 2017 James Drager
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
    definition (name: "ROOMBA switch", namespace: "Elfege", author: "Elfege") {
        capability "Switch"
        capability "Refresh"     
        command "dock"
        command "resume"
        command "pause"
    }
}
// simulator metadata
simulator {
}
// UI tile definitions
tiles {
    standardTile("CLEAN", "device.switch", width: 1, height: 1, canChangeIcon: false) {
        state "released", label: 'Clean', action: "switch.on", icon: "http://cdn.flaticon.com/png/256/56724.png", backgroundColor: "#0088ff", nextState: "pressed"
        //state "pressed", label: 'Cleaning', action: "refresh.refresh", icon: "http://cdn.flaticon.com/png/256/56724.png", backgroundColor: "#F3F781", nextState: "released"
    }
    standardTile("STOP", "device.switch", width: 1, height: 1, canChangeIcon: false) {
        state "released", label: 'Stop', action: "switch.off", icon: "http://cdn.flaticon.com/png/256/56724.png", backgroundColor: "#0088ff", nextState: "pressed"
        state "pressed", label: 'Stopping', action: "", icon: "http://cdn.flaticon.com/png/256/56724.png", backgroundColor: "#F3F781", nextState: "released"
    }
    standardTile("PAUSE", "device.switch", width: 1, height: 1, canChangeIcon: false) {
        state "released", label: 'pause', action: "pause", icon: "http://cdn.flaticon.com/png/256/56724.png", backgroundColor: "#0088ff" //,  nextState: "pressed"      
        state "pressed", label: 'pausing', action: "", icon: "http://cdn.flaticon.com/png/256/56724.png", backgroundColor: "#F3F781", nextState: "released"
    }
    standardTile("RESUME", "device.switch", width: 1, height: 1, canChangeIcon: false) {
        state "released", label: 'Resume', action: "resume", icon: "http://cdn.flaticon.com/png/256/56724.png", backgroundColor: "#0088ff" //,  nextState: "pressed"      
        state "pressed", label: 'resuming', action: "", icon: "http://cdn.flaticon.com/png/256/56724.png", backgroundColor: "#F3F781", nextState: "released"
    }
    standardTile("DOCK", "device.switch", width: 1, height: 1, canChangeIcon: false) {
        state "released", label: 'Dock', action: "dock", icon: "http://cdn.flaticon.com/png/256/56724.png", backgroundColor: "#0088ff", nextState: "docking" 
        state "pressed", label: 'Docking', action: "", icon: "http://cdn.flaticon.com/png/256/56724.png", backgroundColor: "#F3F781", nextState: "on"
    }
    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
        state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
    }        
    main "CLEAN"
    details(["CLEAN","STOP","PAUSE", "RESUME", "DOCK", "refresh"])
}
def parse(description) {
    def msg = parseLanMessage(description)
    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)
}
def sendRequest() {
    state.user = "xxxxxxxxxxxxxxxxxxx"
    state.password = "xxxxxxxxxxxxxxxxxxx"
    state.AssetID = "ElPaso@irobot!xxxxxxxxxxxxxxxxxxx" // replace xxx by your robot’s user name 
    state.Authorization = "xxxxxxxxxxxxxxxxxxxxxxxxxxxx"  // this is a base64 encoding of the string  "user:" + the rotbot’s password  See http://www.url-encode-decode.com/base64-encode-decode/
// Haven't figured out the local command yet   
// state.deviceNetworkId = "728A8678:1BB"  //  "1921681016:443" hex conversion
    //state.ip = "192.168.10.16:443"
    //state.host =  '${state.ip}:443' /*"https://irobot.axeda.com:443" */
    state.path = "/umi/?blid=${state.user}&robotpwd=${state.password}&method=multipleFieldSet&value=%7B%0A%20%20%22remoteCommand%22%20:%20%22${state.RoombaCmd}%22%0A%7D" 
    def httpRequest = [
        method:"GET",
        uri: "https://irobot.axeda.com/services/v1/rest/Scripto/execute/AspenApiRequest?blid=${state.user}&robotpwd=${state.password}&method=multipleFieldSet&value=%7B%0A%20%20%22remoteCommand%22%20:%20%22${state.RoombaCmd}%22%0A%7D",   
        //strictSSL: false,
        headers:	[
            'User-Agent': 'aspen%20production/2618 CFNetwork/758.3.15 Darwin/15.4.0',
            Accept: '*/*',
            'Accept-Language': 'en-us',
            'ASSET-ID': state.AssetID,            
        ]
    ]
    try {
        httpGet(httpRequest) { resp ->
            resp.headers.each {
                log.debug "${it.name} : ${it.value}"
            }
            log.debug "response contentType: ${resp.contentType}"
            log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}
def dock() {
    sendEvent(name: "switch", value: "on")
    log.debug "Roomba Switch is --------------------------docking"
    state.switch  = "pressed"
    log.debug "Running Roomba. Roomba's ID is $state.ID"
    state.RoombaCmd = "dock" 
    sendRequest() 
}
def on() {
    sendEvent(name: "switch", value: "cleaning")
    log.debug "Roomba Switch is --------------------------on"
    state.switch  = "pressed"
    log.debug "Running Roomba"
    state.RoombaCmd = "start" 
    sendRequest() 
}
def resume() {
    sendEvent(name: "switch", value: "resume")
    log.debug "Roomba Switch is --------------------------resume"
    state.switch  = "pressed"
    log.debug "Running Roomba"

    state.RoombaCmd = "resume" 
    sendRequest() 
}
def off() {
    sendEvent(name: "switch", value: "off")
    log.debug "Roomba Switch is --------------------------on"
    state.switch  = "pressed"
    log.debug "Stopping Roomba"

    state.RoombaCmd = "stop" 
    sendRequest() 
}
def pause() {
    sendEvent(name: "switch", value: "off")
    log.debug "Roomba Switch is --------------------------on"
    state.switch  = "pressed"
    log.debug "Running Roomba"
    state.RoombaCmd = "pause" 
    sendRequest() 
}