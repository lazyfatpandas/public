import ast
from astexport.export import export_json


def transform(src, target):
    '''Transforms a Python program into ast and emits it json

      Parameters:
        srcfile: Complete path to the source program
        target: Complete  path for the target file

     Returns:
        0 if the target is generated

     next line is temporary, delete it when stablized'''

    if src is None:
        src = "/home/bhushan/intellijprojects/scirpy/pythonIR/test.py"
    if target is None:
        target = "/home/bhushan/intellijprojects/lasp/1/python-chunking/temp_lasp/temp.json"

    with open(src, "r") as source:
        # with open("test.py", "r") as source:
        tree = ast.parse(source.read())
        f = open(target, "w+")
        json = export_json(tree, "True")
        f.write(json)
        f.close()
    return 0
