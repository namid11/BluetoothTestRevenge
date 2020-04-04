//
//  ViewController.swift
//  TestBluetooth
//
//  Created by NH on 2020/04/01.
//  Copyright © 2020 NH. All rights reserved.
//

import Cocoa

class ViewController: NSViewController {
    
    let bm = BluetoothManager()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        
    }

    override var representedObject: Any? {
        didSet {
        // Update the view, if already loaded.
        }
    }

    @IBAction func runClick(_ sender: Any) {
        bm.run()
    }
    
    @IBAction func readClick(_ sender: Any) {
        bm.readCharacreristic()
    }
    
    @IBAction func notifyClick(_ sender: Any) {
        bm.setNotify()
    }
    
    @IBAction func writeClick(_ sender: Any) {
        bm.write()
    }
}

