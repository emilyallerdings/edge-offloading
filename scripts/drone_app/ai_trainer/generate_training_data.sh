#!/bin/sh

python3 data_convertor.py config.json edge classifier train
python3 data_convertor.py config.json edge classifier test
python3 data_convertor.py config.json edge regression train
python3 data_convertor.py config.json edge regression test
python3 data_convertor.py config.json cloud_rsu classifier train
python3 data_convertor.py config.json cloud_rsu classifier test
python3 data_convertor.py config.json cloud_rsu regression train
python3 data_convertor.py config.json cloud_rsu regression test
python3 data_convertor.py config.json cloud_gsm classifier train
python3 data_convertor.py config.json cloud_gsm classifier test
python3 data_convertor.py config.json cloud_gsm regression train
python3 data_convertor.py config.json cloud_gsm regression test 
python3 data_convertor.py config.json drone classifier train
python3 data_convertor.py config.json drone classifier test
python3 data_convertor.py config.json drone regression train
python3 data_convertor.py config.json drone regression test