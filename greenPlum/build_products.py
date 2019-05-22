from pygresql import pg
import csv

# Connection String
#db = pg.connect('gpadmin', 'localhost', 5432, None, None, 'gpadmin', None)
db = pg.DB (host = '18.213.48.32',dbname='retail',port = 6432,user='retail',passwd='RetailDemo')
# Query Variable
q = db.query('select *   from pivotalmarkets.product order by productid ')

# Query to tuples
qlist=q.getresult()

# Create Header
header = q.listfields()

# Function To Write To CSV
def writeToCSV(results, filename):
        with open(filename, 'wb') as csvfile:
                spamwriter = csv.writer(csvfile, delimiter=',',quotechar='"', quoting=csv.QUOTE_MINIMAL)
                spamwriter.writerow(header)
                for i in results:
                        spamwriter.writerow(i)

# Write to CSV file called products.csv with resultset from qlist
writeToCSV(qlist, 'test-products.csv')


