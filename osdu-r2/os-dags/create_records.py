#  Copyright 2020 Google LLC
#  Copyright 2020 Amazon
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

from osdu_api.model.acl import Acl
from osdu_api.model.legal import Legal
from osdu_api.model.legal_compliance import LegalCompliance
from osdu_api.model.record_ancestry import RecordAncestry
from osdu_api.storage.record_client import RecordClient
from osdu_api.model.record import Record
import json
from airflow.models import Variable


def create_records(**kwargs):
    # the only way to pass in values through the experimental api is through the conf parameter
    data_conf = kwargs['dag_run'].conf

    acl_dict = json.loads(data_conf['acl'])
    acl = Acl(acl_dict['viewers'], acl_dict['owners'])

    legal_dict = json.loads(data_conf['legal-tags'])
    legal = Legal(legal_dict['legaltags'], legal_dict['otherRelevantDataCountries'], LegalCompliance.compliant)
    ancestry = RecordAncestry([])
    record_id = None
    kind = Variable.get('record_kind')
    meta = [{}]
    version = 0
    data = data_conf['data']
    record = Record(record_id, version, kind, acl, legal, data, ancestry, meta)

    headers = {
        'content-type': 'application/json',
        'slb-data-partition-id': data_conf['partition-id'],
        'Authorization': data_conf['authorization'],
        'AppKey': data_conf['app-key']
    }

    record_client = RecordClient()
    resp = record_client.create_update_records([record], headers)

    return {"response_status": resp.status_code, "text": json.loads(resp.text)}
