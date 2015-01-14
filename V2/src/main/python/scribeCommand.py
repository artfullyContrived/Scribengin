import requests, click, json, logging, sys

#r = requests.get("http://www.github.com/DemandCube/Scribengin")
#print r.content
_debug = False
_host = ''
_logfile = ''


@click.group()
@click.option('--debug/--no-debug', default=False, help="Turn debugging on")
@click.option('--host', default='http://127.0.0.1:8181', help="Host to connect to")
@click.option('--logfile', default='scribeCommand.log', help="Log file to write to")
def mastercommand(debug, host, logfile):
    global _debug, _host, _logfile
    _debug = debug
    _host = host
    _logfile = logfile
    if _debug:
      #Set logging file, overwrite file, set logging level to DEBUG
      logging.basicConfig(filename=_logfile, filemode="w", level=logging.DEBUG)
      logging.getLogger().addHandler(logging.StreamHandler(sys.stdout))
      click.echo('Debug mode is %s' % ('on' if debug else 'off'))
    else:
      #Set logging file, overwrite file, set logging level to INFO
      logging.basicConfig(filename=_logfile, filemode="w", level=logging.INFO)



@mastercommand.command(help="- List all running VMs")
def listvms():
  logging.debug("Listing VMs")
  payload = {'command': 'listvms'}
  res = sendRequest(payload)
  click.echo(res.text)


def sendRequest(params):
    global _host
    logging.debug("Sending Request")
    logging.debug("HOST: " + _host)
    logging.debug("PARAMETERS: " + str(params))
    
    try:
      r = requests.post(_host, data=params)
    except Exception, e:
      return "Error handling request: "+str(e)
    return r


if __name__ == '__main__':
  mastercommand()

  