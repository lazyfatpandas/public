import ast
from astexport.export import export_json
def main():
    with open("test.py", "r") as source:
    #with open("test.py", "r") as source:
        tree = ast.parse(source.read())
        f= open("../temp.json","w+")
        json = export_json(tree, "True")
        f.write(json)

        # f.write("End of JSON")
        # f.write(ast.dump(tree))

        f.close()

if __name__ == "__main__":
    main()
