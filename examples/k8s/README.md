# Notes

Initial conversion: 

```bash
$ curl -L https://github.com/kubernetes/kompose/releases/download/v1.18.0/kompose-darwin-amd64 -o kompose
$ sudo mv kompose /usr/local/bin/
$ sudo chmod 755 /usr/local/bin/kompose 
$ kompose convert -f docker-compose-prod.yml
```
Cleanup resulting yaml files, remove superfluous elements.

Put `wait-for-it.sh` in a ConfigMap, so that it can be mounted read-only by different deployments simultaneously.

Kubernetes cannot mount files in a directory without masking the other content of the directory, so had to mount `wait-for-it.sh` in a subdirectory.

Created `textrepo-app-ingress.yaml` manually so that the application is reachable from the outside world under a domain name.

Deployment:

```bash
$ kubectl --kubconfig=$HOME/.kube/<configfile> -n textrepo apply -f yaml/
```

Copied data to "/about" and "/concordion" in the nginx deployment:

```bash
$ kubectl --kubeconfig=$HOME/.kube/<configfile> cp ~/about textrepo/nginx-654869df7d-gqfc4:/about
$ kubectl --kubeconfig=$HOME/.kube/<configfile> cp ~/concordion textrepo/nginx-654869df7d-gqfc4:/concordion
```

and executed `start.sh` manually. Todo: use an `initContainer` in the nginx deployment for this.
