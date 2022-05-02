from django.db import models


class Record(models.Model):
    boreholenumber = models.CharField(max_length=10, blank=True, null=True)
    instrument = models.CharField(max_length=20, blank=True, null=True)
    surfacelevel = models.FloatField(blank=True, null=True)
    northing = models.BigIntegerField(blank=True, null=True)
    easting = models.BigIntegerField(blank=True, null=True)
    reading = models.FloatField(blank=True, null=True)
    ts = models.CharField(max_length=30, blank=True, null=True)

    class Meta:
        managed = False
        db_table = 'record'
