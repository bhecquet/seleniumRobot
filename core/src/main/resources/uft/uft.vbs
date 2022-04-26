' --------------------------------------------
' Script that allows to launch UFT test
'
' Launch ALM test:
' ex: cscript.exe uft.vbs [QualityCenter]Subject\xxx\xxx\test1 /load /execute /server:http://<host>:<port>/qcbin /user:myuser /password:mypassword /domain:mydomain /project:myproject
'
' Load Script and then execute
' cscript.exe uft.vbs [QualityCenter]Subject\xxx\xxx\test1 /load /server:http://<host>:<port>/qcbin /user:myuser /password:mypassword /domain:mydomain /project:myproject
' cscript.exe uft.vbs [QualityCenter]Subject\xxx\xxx\test1 /execute

' 
' Launch Local UFT test:
' ex: cscript.exe uft.vbs <path_to_test>
'
' Scripts parameters can be set using format "key=value" and are associated to "/execute" flag
' In the following, we start test1 with parameter 'User' that takes value 'foo'
' ex: cscript.exe uft.vbs [QualityCenter]Subject\xxx\xxx\test1 /load /execute User=foo /server:http://<host>:<port>/qcbin /user:myuser /password:mypassword /domain:mydomain /project:myproject
' 
' An optional parameter '/output:<result_folder>' can be given to set the folder where result will be written (defaults to D:\uft\output)
' An optional parameter '/clean' can be given so that UFT is killed before being restarted
' An optional parameter '/load' will load the script only
' An optional parameter '/execute' will execute the script only. Assume the test has been loaded. At the end of the execution, UFT is stopped
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
' https://admhelp.microfocus.com/uft/en/all/AutomationObjectModel/Content/QuickTest~RunOptions~RunMode~Configure%20a%20Test%20to%20Run%20in%20Fast%20Mode_E.html 


Test_path = Wscript.Arguments(0)
'Test_path = "[QualityCenter]Subject\xxx\yyyy\test1" for ALM tests
Set qtApp = CreateObject("QuickTest.Application") ' Create the Application object
qtApp.Launch ' Start UFT
qtApp.Visible = False ' Make the QuickTest application not visible

'' Set QuickTest run options
qtApp.Options.Run.RunMode = "Normal"
qtApp.Options.Run.ViewResults = False



' Open the test in read-only mode
If Wscript.Arguments.Named.Exists("load") Then

    'connection to ALM
    If qtApp.TDConnection.IsConnected Then
        WScript.Echo "Disconnect from ALM" 
        qtApp.TDConnection.disconnect
    End If
    
    ' Connect to ALM only if required
    If (Wscript.Arguments.Named.Exists("server") And Wscript.Arguments.Named.Exists("user") And Wscript.Arguments.Named.Exists("password")And Wscript.Arguments.Named.Exists("domain")  And Wscript.Arguments.Named.Exists("project")   ) Then
        Dim server, user, password, domain, project
    
        server = Wscript.Arguments.Named.Item("server")
        user = Wscript.Arguments.Named.Item("user")
        password = Wscript.Arguments.Named.Item("password")
        domain = Wscript.Arguments.Named.Item("domain")
        project = Wscript.Arguments.Named.Item("project")
        
        WScript.Echo "Connecting to ALM[" + server + "] with user '" + user + "'"
        qtApp.TDConnection.Connect server, domain, project, user, password, False
        
    End If

    ' Open test script in read only
    qtApp.Open Test_path, True  
End If
    
' Execute the test. It assumes a test is loaded
If Wscript.Arguments.Named.Exists("execute") Then
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

    ' Declare a Run Results Options object variable
    Dim qtResultsOpt 'As QuickTest.RunResultsOptions 
    
    ' Create the Run Results Options object
    Set qtResultsOpt = CreateObject("QuickTest.RunResultsOptions") 
    
    ' Set the results location
    qtResultsOpt.ResultsLocation = resultFolder 
    
    ' set run settings for the test
    Set qtTest = qtApp.Test
    
    ' Run the test
    qtTest.Run qtResultsOpt, true, rtParams 	
    
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
End If

wscript.quit 0

