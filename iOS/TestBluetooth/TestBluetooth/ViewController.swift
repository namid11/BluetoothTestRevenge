//
//  ViewController.swift
//  TestBluetooth
//
//  Created by NH on 2020/04/04.
//  Copyright © 2020 NH. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    let bluetoothManager = BluetoothManager()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
    }

    @IBAction func send(_ sender: Any) {
        bluetoothManager.advertisement()
    }
    
    @IBAction func update(_ sender: Any) {
        bluetoothManager.update()
    }
    
}

