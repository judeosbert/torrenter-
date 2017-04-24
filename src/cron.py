import mysql.connector

db = mysql.connector.connect(user='root',password='root',host='127.0.0.1',database='torrents')

cursor = db.cursor()

cursor.execute("UPDATE swarms SET seederOnline = 0 WHERE 1 ")
db.commit()
db.close()
