# Import all the models, so that Base has them before being
# imported by Alembic or used to create tables
from app.db.session import Base  # noqa
from app.models.user import User  # noqa
from app.models.device import Device  # noqa
from app.models.session import DrivingSession  # noqa
from app.models.alert import Alert  # noqa
from app.models.location import LocationPoint  # noqa
