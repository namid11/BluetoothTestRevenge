//
//  BluetoothManager.swift
//  TestBluetooth
//
//  Created by NH on 2020/04/04.
//  Copyright © 2020 NH. All rights reserved.
//

import Foundation
import CoreBluetooth

let serviceUUID = CBUUID(string: "4627f78e-7410-11ea-bc55-0242ac130003")
let characreristicParamUUID = CBUUID(string: "b20a1840-676b-41ff-8947-7543108499d5")

class BluetoothManager: NSObject, CBPeripheralManagerDelegate {
    
    var count = 0
    
    let characteristic = CBMutableCharacteristic(
        type: characreristicParamUUID,
        properties: CBCharacteristicProperties.notify.union(.read).union(.write),
        value: nil,
        permissions: CBAttributePermissions.readable.union(.writeable))
    let service = CBMutableService(type: serviceUUID, primary: true)
    var peripheralManager: CBPeripheralManager? = nil
    var central: CBCentral? = nil
    
    override init() {
        super.init()
        self.peripheralManager = CBPeripheralManager.init(delegate: self, queue: nil)
    }
    
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .poweredOn:
            print("POWER ON")
            self.service.characteristics = [characteristic]
            self.peripheralManager?.add(service)

        case .poweredOff:
            print("POWER OFF")
        default:
            print("STATE:", peripheral.state)
        }
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, didAdd service: CBService, error: Error?) {
        if let e = error {
            print(e)
            return
        }
        
        print("succeeded to add service")
    }
    
    func peripheralManagerDidStartAdvertising(_ peripheral: CBPeripheralManager, error: Error?) {
        if let e = error {
            print(e)
            return
        }
        
        print("succeeded to start advertising")
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral, didSubscribeTo characteristic: CBCharacteristic) {
        print("Subscribe to", characteristic.uuid)
        self.central = central
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveRead request: CBATTRequest) {
        print("read request")
        request.value = "HELLO BLUETOOTH".data(using: .utf8) ?? Data()
        peripheralManager?.respond(to: request, withResult: CBATTError.success)
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveWrite requests: [CBATTRequest]) {
        print("write request")
        for req in requests {
            print("DATA:",String(data: req.value ?? Data(), encoding: .utf8) ?? "")
            peripheralManager?.respond(to: req, withResult: CBATTError.success)
        }
    }
    
    func advertisement() {
        let advertisementData = [
            CBAdvertisementDataServiceUUIDsKey: [service.uuid],
            CBAdvertisementDataLocalNameKey: "MyIOS"] as [String : Any]
        peripheralManager?.startAdvertising(advertisementData)
    }
    
    
    func update() {
        count += 1
        let updateValue = String(count).data(using: .utf8) ?? Data()
        characteristic.value = updateValue
        let result = peripheralManager?.updateValue(
            updateValue,
            for: characteristic,
            onSubscribedCentrals: nil)
        print("result:", result)
    }
    
}
