#!/usr/bin/env python3

import httpx
import asyncio
import time

limit = 100

async def getAndVerifyArtifact(client, httpVersion, url):
		#print(url)
		response = await client.get(url)
		if response.status_code != 200 or response.http_version != httpVersion:
			print("Failed to download", url, response.status_code, response.http_version)
		#else:
		#	print(response.content)
	


async def downloadArtifacts(client, urls, httpVersion):
    tasks = [asyncio.ensure_future(getAndVerifyArtifact(client, httpVersion, url)) for url in urls]
    await asyncio.gather(*tasks)

def loadArtifactUrls(filename):
	print("Loading test file...")
	
	artifactUrls=[]
	with open(filename) as urlsFile:
		for line in urlsFile:
			artifactUrls.append(line.strip())
			
	print("Loaded", len(artifactUrls), "artifact URLs")
	
	return artifactUrls

async def measureDownloadTime(client, artifactUrls, protocol):
	before = int(round(time.time() * 1000))
	await downloadArtifacts(client, artifactUrls, protocol)
	after = int(round(time.time() * 1000))
	return after - before
	

async def testHttp1Performance(artifactUrls):
	print("Testing HTTP/1.1 performance with limit of", limit, "artifacts...")
	
	async with httpx.AsyncClient(http2=False) as client:
		duration = await measureDownloadTime(client, artifactUrls[:limit], "HTTP/1.1")
	
	print("Artifacts were downloaded using HTTP/1.1 in", duration, "ms")
	

async def testHttp2Performance(artifactUrls):
	print("Testing HTTP/2.0 performance with limit of", limit, "artifacts...")
	
	async with httpx.AsyncClient(http2=True) as client:
		duration = await measureDownloadTime(client, artifactUrls[:limit], "HTTP/2")
	
	print("Artifacts were downloaded using HTTP/2.0 in", duration, "ms")


if __name__ == '__main__':
	artifactUrls = loadArtifactUrls("centralUrls.txt")
	
	loop = asyncio.get_event_loop()
	loop.run_until_complete(testHttp1Performance(artifactUrls))
	
	loop = asyncio.get_event_loop()
	loop.run_until_complete(testHttp2Performance(artifactUrls))
