#This is how to use NRSerialPort objects

<pre>
NRSerialPort serial = new NRSerialPort("COM3", 115200);<br>
<br>
serial.connect();<br>
<br>
DataInputStream ins = new DataInputStream(serial.getInputStream());<br>
<br>
DataOutputStream outs = new DataOutputStream(serial.getOutputStream());<br>
<br>
byte b = ins.read();<br>
<br>
outs.write(b);<br>
<br>
serial.disconnect();<br>
<br>
</pre>