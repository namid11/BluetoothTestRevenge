//
//  BluetoothManager.swift
//  TestBluetooth
//
//  Created by NH on 2020/04/01.
//  Copyright © 2020 NH. All rights reserved.
//

import Foundation
import CoreBluetooth

// MACはCentral（データを利用する側）です。一応クライアント？
// スマホはPeripheral（データを渡す側）。一応サーバ？

class BluetoothManager: NSObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    var cbCentralManager: CBCentralManager!
    var targetPeripheral: CBPeripheral? = nil
    var writeCharacteristic: CBCharacteristic? = nil
    var targetDescriptor: CBDescriptor? = nil
    let serviceUUID = [CBUUID(string: "4627f78e-7410-11ea-bc55-0242ac130003")]
    let characreristicParamUUID = CBUUID(string: "b20a1840-676b-41ff-8947-7543108499d5")
    let notificationUUID = CBUUID(string: "cd88aee8-74ed-11ea-bc55-0242ac130003")
    
    override init () {
        super.init()
        cbCentralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOff:
            print("POWERED OFF")
            break
        case .poweredOn:
            print("POWERED ON")
            break
        case .resetting:
            print("RESETTING")
            break
        case .unauthorized:
            print("UN Authorized")
            break
        case .unknown:
            print("unknown")
            break
        case .unsupported:
            print("not supported")
            break
        default:
            print("?")
        }
    }

    func run() {
        if (!cbCentralManager.isScanning) {
            print("スキャンしまーす")
            cbCentralManager.scanForPeripherals(withServices: serviceUUID, options: nil)  // データを渡す周辺機器（サーバ）を探索。見つかったらcentralManager(deidDiscover)が呼び出される
        } else {
            print("スキャン中です")
        }

    }
    
    // サーバが見つかったら呼び出される
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        print("サーバが見つかったで")
        self.targetPeripheral = peripheral
        self.cbCentralManager.connect(self.targetPeripheral!, options: nil)
        cbCentralManager.stopScan()
    }
    
    // サーバと接続成功したら呼び出される
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        print("サーバと接続したで")
        targetPeripheral?.delegate = self  // サーバから何かある度にイベントを受け取れるようにデリゲートをセット
        targetPeripheral?.discoverServices(serviceUUID)
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("Disconnect")
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        print("connection failed")
    }
    
    func peripheralIsReady(toSendWriteWithoutResponse peripheral: CBPeripheral) {
        print("Send Writing")
    }
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        if error != nil {
            print("残念")
            print(error!)
        }
        
        print("write value", characteristic.value ?? "NULL")
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if error != nil {
            print("残念")
            print(error!)
        }
        
        print("サービスあったで", peripheral.services?.count ?? 0)
        for service in peripheral.services ?? [] {
            if(service.uuid.uuidString == serviceUUID[0].uuidString) {
                print("属性データ探しまーす")
                targetPeripheral?.discoverCharacteristics(nil, for: service)
             }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        print("属性データ発見！", service.characteristics!.count)
        for characreristic in service.characteristics!{
//            if characreristic.uuid.uuidString == "属性がNotifyのキャラクタリスティックのUUID" {
//                //Notificationを受け取るハンドラ
//                peripheral.setNotifyValue(true, for: characreristic)
//            }
//
            if characreristic.uuid == notificationUUID {
                self.targetPeripheral?.setNotifyValue(true, for: characreristic)
            }
            
            if characreristic.uuid == characreristicParamUUID {
                self.writeCharacteristic = characreristic
                self.targetPeripheral?.discoverDescriptors(for: characreristic)
            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        if let e = error {
            print("Error: on update value", e)
        }
        
        if let data = characteristic.value {
            print("DATA Characteristic:", String(data: data, encoding: .utf8) ?? "NONE")
        } else {
            print("DATA Characteristic: NONE")
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        if let e = error {
            print("Error: on update notification state for")
            print(e)
            return
        }

        print("OK: on update notification state for")
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor descriptor: CBDescriptor, error: Error?) {
        print("DATA Descriptor:", (descriptor.value as? String) ?? "")
    }
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor descriptor: CBDescriptor, error: Error?) {
        print("write value")
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverDescriptorsFor characteristic: CBCharacteristic, error: Error?) {
        print("discover descriptor")
        for descriptor in characteristic.descriptors ?? [] {
//            if descriptor.uuid == notificationUUID {
//                self.targetDescriptor = descriptor
//                self.targetPeripheral?.writeValue(
//                    String(CBCharacteristicProperties.notify.rawValue).data(using: .utf8) ?? Data(),
//                    for: descriptor)
////                self.targetPeripheral?.setNotifyValue(true, for: characteristic)
//            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverIncludedServicesFor service: CBService, error: Error?) {
        print("discover incluced")
    }
    
    func peripheralDidUpdateName(_ peripheral: CBPeripheral) {
        print("changed peripheral name :", peripheral.name ?? "")
    }
    
    func readCharacreristic() {
        let thread = Thread.init {
            if let c = self.writeCharacteristic {
                self.targetPeripheral?.readValue(for: c)
//                self.targetPeripheral?.writeValue(Data(base64Encoded: "HELLO") ?? Data(), for: c, type: .withResponse)
            } else {
                
            }
        }
        thread.start()
    }

    
    func setNotify() {
        self.targetPeripheral?.setNotifyValue(true, for: writeCharacteristic!)
    }
    
    func write() {
        self.targetPeripheral?.writeValue("ABC".data(using: .utf8) ?? Data(), for: writeCharacteristic!, type: .withResponse)
        print(self.writeCharacteristic!.isNotifying)
        print(self.writeCharacteristic!.properties)
        print(CBCharacteristicProperties.notify)
        print(CBCharacteristicProperties.indicate)
    }
}
