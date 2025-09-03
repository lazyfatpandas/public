import os
import subprocess
import os
from py4j.java_gateway import JavaGateway
from scirpy_interface.source_transformer import transform
from estimator.base import estimator


def analyze_source(file_path):
    file_name = os.path.splitext(os.path.basename(file_path))[0]
    directoryname = os.path.dirname(file_path + "")

    print("Performing analysis on file:\n", file_path, "\n", file_name)
    # Pass source and target for transforming to json, the json file is generated in JSON/temp.json

    # source_name_full_json = os.path.join(directoryname, 'JSON', 'temp.json')
    # mar 16,2024 bhu changed--not possible to create json folder in each folder
    source_name_full_json = os.path.join(directoryname, 'temp.json')
    transform(file_path, source_name_full_json)

    gateway = JavaGateway()                   # connect to the JVM
    scripy = gateway.entry_point               # get the AdditionApplication instance
    # sourceNameFull,sourceFileName,destinationPath

    # "/home/bhu/intellijprojects/lasp/1/python-chunking/temp_lasp/"
    # mar 16,2024 bhu changed--not possible to create json folder in each folder
    # opt_path = os.path.join(directoryname, "temp_lasp", "")
    opt_path = os.path.join(directoryname, "")

    scripy.runIPMain(source_name_full_json, file_name, opt_path, directoryname)
    result = 0

    opt_path_full = opt_path+"Opt_Code_"+file_name+".py"
    print(opt_path_full)
    # opt_path = "D:\\MTP\\python-chunking\\temp_lasp\\Opt_Code_"+file_name+".py"
    # cmd="python3 " + opt_path
    # if result==0:
    #     os.system(cmd)
    #     exit(0)
    return (opt_path_full, result)


def run_optimized(opt_file_with_result):
    cmd = "python3 " + opt_file_with_result[0]
    if opt_file_with_result[1] == 0:
        os.system(cmd)
        exit(0)
