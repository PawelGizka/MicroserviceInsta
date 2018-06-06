
from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('UserManager', '0001_initial'),
    ]

    operations = [
        migrations.RenameField(
            model_name='token',
            old_name='text',
            new_name='token',
        ),
        migrations.RemoveField(
            model_name='token',
            name='expire',
        ),
        migrations.AlterField(
            model_name='token',
            name='user',
            field=models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL),
        ),
    ]
