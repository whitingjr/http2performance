#!/usr/bin/env python3

import httpx
import asyncio
import timeit

async def getAndVerifyArtifact(client, httpVersion, url):
		#print(url)
		response = await client.get(url)
		#print(response.status_code, response.http_version)
		if response.status_code != 200 or response.http_version != httpVersion:
			print("Failed to download", url, response.status_code, response.http_version)
		else:
			print(response.content)
	


async def downloadArtifacts(client, urls, httpVersion):
    tasks = [asyncio.ensure_future(getAndVerifyArtifact(client, httpVersion, url)) for url in urls]
    p = asyncio.ensure_future(progress(tasks))
    await asyncio.gather(*tasks, p)

def loadArtifactUrls(filename):
	print("Loading test file...")
	
	artifactUrls=[]
	with open(filename) as urlsFile:
		for line in urlsFile:
			artifactUrls.append(line.strip())
			
	print("Loaded", len(artifactUrls), "artifact URLs")
	
	return artifactUrls

async def testHttp1Performance(artifactUrls):
	print("Testing HTTP/1.1 performance...")
	
	client = httpx.AsyncClient(http2=False)
	duration1 = timeit.timeit(lambda: downloadArtifacts(client, artifactUrls[:10], "HTTP/1.1"), number=1)
	
	print("Artifacts were downloaded using HTTP/1.1 in", duration1)

async def testHttp2Performance(artifactUrls):
	print("Testing HTTP/2.0 performance...")
	
	client = httpx.AsyncClient(http2=True)
	duration1 = timeit.timeit(lambda: downloadArtifacts(client, artifactUrls[:10], "HTTP/2"), number=1)
	
	print("Artifacts were downloaded using HTTP/2.0 in", duration1)


if __name__ == '__main__':
	artifactUrls = loadArtifactUrls("centralUrls.txt")
	
	loop = asyncio.get_event_loop()
	loop.run_until_complete(testHttp1Performance(artifactUrls))

	loop = asyncio.get_event_loop()
	loop.run_until_complete(testHttp2Performance(artifactUrls))
	
