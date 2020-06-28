import math
from datetime import datetime

import numpy as np
import pandas as pd
import requests


def convert_to_statistics(df):
    # calculate change percentage
    df_change = df.transpose().pct_change()
    df_change = df_change.replace(np.inf, 1)

    data = []
    for country in df.index:
        aggregated_stat = {
            'country': country,
            'values': {}
        }
        for date in list(df.columns.values):
            formatted_date = datetime.strptime(date, '%m/%d/%y').strftime('%Y-%m-%d')
            pct_change = float(df_change[country][date])
            aggregated_stat['values'][formatted_date] = {
                "value": int(df[date][country]),
                "pctChange": pct_change if not math.isnan(pct_change) else None
            }
        data.append(aggregated_stat)
    return data


def load_sick(confirmed, deaths, recovered):
    return confirmed - deaths - recovered


def load_file(file):
    try:
        df = pd.read_csv(f'{source_url}/{file}')
        return df.set_index('country')
    except Exception as e:
        print(e)
    return None


def send_data(data, target):
    responses = [requests.post(f'{target_server}{target}/python', json=json).status_code for json in data]
    result = {}
    for code in set(responses):
        result[code] = responses.count(code)
    print(f"{target}: {result}")


if __name__ == '__main__':
    source_url = 'https://raw.githubusercontent.com/jjbeto/talkPy4JavaDevResources/master/johns_hopkins'
    confirmed_file = 'time_series_covid19_confirmed_global.csv'
    deaths_file = 'time_series_covid19_deaths_global.csv'
    recovered_file = 'time_series_covid19_recovered_global.csv'

    target_server = 'http://localhost:8080/api/'

    confirmed = load_file(confirmed_file)
    deaths = load_file(deaths_file)
    recovered = load_file(recovered_file)
    sick = load_sick(confirmed, deaths, recovered)

    confirmed_json = convert_to_statistics(confirmed)
    deaths_json = convert_to_statistics(deaths)
    recovered_json = convert_to_statistics(recovered)
    sick_json = convert_to_statistics(sick)

    send_data(confirmed_json, 'confirmed')
    send_data(deaths_json, 'deaths')
    send_data(recovered_json, 'recovered')
    send_data(sick_json, 'sick')
