//
//  ReceiveManager.swift
//  TestBluetooth
//
//  Created by NH on 2020/04/02.
//  Copyright © 2020 NH. All rights reserved.
//

import Foundation
import CoreBluetooth

class ReceiveManager: NSObject, CBPeripheralManagerDelegate {
    
    let myCustomServiceUUID = CBUUID(string: "4627f78e-7410-11ea-bc55-0242ac130003")
    let myCharacteristicUUID = CBUUID(string: "4627f78e-7410-11ea-bc55-0242ac130003")
    
    var cbPeripheralManager: CBPeripheralManager!
    
    override init() {
        super.init()
        
        cbPeripheralManager = CBPeripheralManager(delegate: self, queue: nil)
    }
    
    func run() {
        cbPeripheralManager.startAdvertising(["TestBluetooth" : CBUUID(string: "4627f78e-7410-11ea-bc55-0242ac130003")])
    }
    
    
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .poweredOn:
            print("POWERED ON")
            break
        case .poweredOff:
            print("POWERED OFF")
            break
        case .resetting:
            print("RESETTING")
            break
        case .unauthorized:
            print("UNAUTHORIZED")
            break
        case .unknown:
            print("UNKNOWN")
            break
        case .unsupported:
            print("UN SUPPORTED")
            break
        default:
            print("?")
        }
    }
    
    func peripheralManagerDidStartAdvertising(_ peripheral: CBPeripheralManager, error: Error?) {
        if let e = error {
            print(e)
            return
        }
        
        print("Peripheral Advertising OK")
    }
 
    func peripheralManager(_ peripheral: CBPeripheralManager, didOpen channel: CBL2CAPChannel?, error: Error?) {
        print("OPEN L2CAP CHANNEL")
    }
}
