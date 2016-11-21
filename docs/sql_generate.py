from random import *
filename = "large.sql"
def generator():
	def f(i):
		return str(i) + ", " + str(i) + ", '" + 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'[randint(0, 25)] + "', " + generate_price() + ", '" + generate_clerk() + "', '" + generate_comment(i) + "'"

	def generate_price():
		return str(randint(0, 999999))

	def generate_clerk():
		return "Clerk#" + str(randint(0, 9999))

	def generate_comment(i):
		letter_tuple = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
		result = ''
		for i in range(randint(5, 50)):
			result += letter_tuple[randint(0, 26-1)]
		return result+str(i)

	for i in range(200000):
		print("insert into orders values(" + f(i) + ");", file=open(filename, mode='a'))
	print('ok')


generator()
