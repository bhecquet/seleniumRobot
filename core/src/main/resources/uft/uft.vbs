' --------------------------------------------
' Script that allows to launch UFT test
'
' Launch ALM test:
' ex: cscript.exe uft.vbs [QualityCenter]Subject\xxx\xxx\test1 /server:http://<host>:<port>/qcbin /user:myuser /password:mypassword /domain:mydomain /project:myproject
'
' Launch Local UFT test:
' ex: cscript.exe uft.vbs <path_to_test>
'
' Scripts parameters can be set using format "key=value"
' In the following, we start test1 with parameter 'User' that takes value 'foo'
' ex: cscript.exe uft.vbs [QualityCenter]Subject\xxx\xxx\test1 User=foo /server:http://<host>:<port>/qcbin /user:myuser /password:mypassword /domain:mydomain /project:myproject
' 
' An optional parameter '/output:<result_folder>' can be given to set the folder where result will be written (defaults to D:\uft\output)
' An optional parameter '/clean' can be given so that UFT is killed before being restarted
' --------------------------------------------


Sub DeleteOutput(folder) 
	Dim fso   
	Set fso=createobject("Scripting.FileSystemObject")
	If fso.FolderExists(folder) Then
		fso.DeleteFolder folder,True
	End If
End Sub

Sub KillAll(ProcessName)
    Dim objWMIService, colProcess
    Dim strComputer, strList, p
    strComputer = "."
    Set objWMIService = GetObject("winmgmts:" & "{impersonationLevel=impersonate}!\\" & strComputer & "\root\cimv2") 
    Set colProcess = objWMIService.ExecQuery ("Select * from Win32_Process Where Name like '" & ProcessName & "'")
    For Each p in colProcess
        p.Terminate             
    Next
End Sub

If Wscript.Arguments.Named.Exists("output") Then
	resultFolder = Wscript.Arguments.Named.Item("output")
Else
	resultFolder = "D:\uft\output"
WScript.Echo "Result file written to: " + resultFolder + "\Report\Results.xml"

End If
On Error Resume Next
DeleteOutput(resultFolder)

' Kill existing UFT processes
If Wscript.Arguments.Named.Exists("clean") Then
    KillAll("uft.exe")
End If

'---------------------------------------------------------------------------------'

Test_path = Wscript.Arguments(0)
'Test_path = "[QualityCenter]Subject\xxx\yyyy\test1" for ALM tests
Set qtApp = CreateObject("QuickTest.Application") ' Create the Application object
qtApp.Launch ' Start QuickTest
qtApp.Visible = True ' Make the QuickTest application visible

'' Set QuickTest run options
qtApp.Options.Run.RunMode = "Normal"
qtApp.Options.Run.ViewResults = False

'connection à QC
If qtApp.TDConnection.IsConnected Then
	WScript.Echo "Disconnect from ALM" 
	qtApp.TDConnection.disconnect
End If

' Connect to ALM only if required
If (Wscript.Arguments.Named.Exists("server") And Wscript.Arguments.Named.Exists("user") And Wscript.Arguments.Named.Exists("password")And Wscript.Arguments.Named.Exists("domain")	And Wscript.Arguments.Named.Exists("project")	) Then
	Dim server, user, password, domain, project

	server = Wscript.Arguments.Named.Item("server")
	user = Wscript.Arguments.Named.Item("user")
	password = Wscript.Arguments.Named.Item("password")
	domain = Wscript.Arguments.Named.Item("domain")
	project = Wscript.Arguments.Named.Item("project")
	
	WScript.Echo "Connecting to ALM[" + server + "] with user '" + user + "'"
	qtApp.TDConnection.Connect server, domain, project, user, password, False
	
End If

qtApp.Open Test_path, False ' Open the test in read-only mode

Set pDefColl = qtApp.Test.ParameterDefinitions
Set rtParams = pDefColl.GetParameters()
Dim keyValue

For Each strArg in Wscript.Arguments
	keyValue = Split(strArg, "=")
	
	cnt = pDefColl.Count
	Indx = 1
	While Indx <= cnt
		Set pDef = pDefColl.Item(Indx)
		Indx = Indx + 1
		If StrComp(pDef.Name, keyValue(0)) = 0 Then
			Set rtParam2 = rtParams.Item(keyValue(0))
			rtParam2.Value = keyValue(1)
		End If
		
	Wend
	
Next

Dim qtResultsOpt 'As QuickTest.RunResultsOptions ' Declare a Run Results Options object variable

Set qtResultsOpt = CreateObject("QuickTest.RunResultsOptions") ' Create the Run Results Options object
qtResultsOpt.ResultsLocation = resultFolder ' Set the results location

' set run settings for the test
Set qtTest = qtApp.Test
qtTest.Run qtResultsOpt, true, rtParams 	' Run the test

Dim objStream, strData
Set objStream = CreateObject("ADODB.Stream")
objStream.CharSet = "utf-8"
objStream.Open
objStream.LoadFromFile(resultFolder + "\Report\Results.xml")
content = objStream.ReadText()
objStream.Close
Set objStream = Nothing

WScript.Echo "_____OUTPUT_____"
WScript.Echo content
WScript.Echo "_____ENDOUTPUT_____"

qtTest.Close			' Close the test
qtApp.quit
Set qtTest = Nothing		' Release the Test object
Set qtApp = Nothing 		' Release the Application object 
Set qtResultsOpt = Nothing

wscript.quit 0

