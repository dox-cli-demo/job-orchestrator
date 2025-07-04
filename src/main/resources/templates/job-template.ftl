apiVersion: batch/v1
kind: Job
metadata:
  name: print-job-${requestId}-${taskId}
spec:
  ttlSecondsAfterFinished: 120
  template:
    spec:
      containers:
        - name: printer
          image: alpine
          command:
            - sh
            - -c
            - echo "Data for partition ${taskId}" > /mnt/nfs/output/${requestId}/locationdata/${taskId}.txt
          volumeMounts:
            - name: nfs-volume
              mountPath: /mnt/nfs/output
      restartPolicy: Never
      volumes:
        - name: nfs-volume
          persistentVolumeClaim:
            claimName: nfs-pvc