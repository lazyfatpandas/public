import re
class student:
    def __init__(self,cid,cname,email,result):
        self.studentId = cid
        self.studentName = cname
        self.studentEmail = email
        self.studentresult = result

class studentDirectory:
    def __init__(self,did,cList):
        self.directoryId = did
        self.studentList = cList


    def findCGPAFromresult(self):
        result=[]
        for student in self.studentList:
            pin=re.search(r'(\d{6})',student.studentresult)
            if pin is not None:
                result.append((student.studentId,pin.group()))
        return result

    def countEmailsOfGivenDomain(self,domain):
        count=0
        for student in self.studentList:
            if(student.studentEmail.split('@')[1]==domain):
                count=count+1
        return count

if __name__=='__main__':
    count = int(input())
    students = []
    for i in range(count):
        cid = int(input())
        cname = input()
        cemail = input()
        cresult = input()
        students.append(student(cid,cname,cemail,cresult))
    inpDomain = input()
    cd = studentDirectory(100,students)
    output = cd.findCGPAFromresult()
    for out in output:
        print(out[0],out[1])
    print("Count : ",cd.countEmailsOfGivenDomain(inpDomain))